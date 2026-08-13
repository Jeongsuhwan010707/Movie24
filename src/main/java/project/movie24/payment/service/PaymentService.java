package project.movie24.payment.service;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.movie24.coupon.domain.CouponApplicableContext;
import project.movie24.coupon.domain.UserCoupon;
import project.movie24.coupon.service.UserCouponService;
import project.movie24.payment.client.TossPaymentClient;
import project.movie24.payment.domain.PendingPayment;
import project.movie24.payment.dto.PaymentPrepareRequest;
import project.movie24.payment.dto.PaymentPrepareResponse;
import project.movie24.reservation.domain.Reservation;
import project.movie24.reservation.service.ReservationService;
import project.movie24.seat.domain.Seat;
import project.movie24.seat.service.SeatService;
import project.movie24.showtime.domain.Showtime;
import project.movie24.showtime.service.ShowtimeService;
import project.movie24.store.domain.TicketVoucher;
import project.movie24.store.service.TicketVoucherService;
import project.movie24.user.domain.User;
import project.movie24.user.repository.UserRepository;
import project.movie24.user.service.UserService;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final String SESSION_KEY = "PENDING_PAYMENT";
    // 포인트로 전액을 대체하면 Toss 위젯의 0원 결제라는 별도 케이스를 다뤄야 하므로,
    // 캐시로 결제할 최소 금액을 남겨두도록 포인트 사용 한도를 제한한다.
    private static final int MIN_PAYABLE_AMOUNT = 100;

    private final ShowtimeService showtimeService;
    private final SeatService seatService;
    private final ReservationService reservationService;
    private final TossPaymentClient tossPaymentClient;
    private final UserRepository userRepository;
    private final UserService userService;
    private final UserCouponService userCouponService;
    private final TicketVoucherService ticketVoucherService;

    @Transactional(readOnly = true)
    public PaymentPrepareResponse prepare(HttpSession session, Long userId, PaymentPrepareRequest request) {
        Showtime showtime = showtimeService.findOne(request.getShowtimeId());
        List<Seat> seats = seatService.findAllByIdsOrThrow(request.getSeatIds());
        int totalPrice = showtime.priceFor(seats.size());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // 등급 할인(%)과 쿠폰(%/금액)은 "할인"이라 원가를 순서대로 줄여가며 계산하고,
        // 관람권/기프티콘과 포인트는 "결제수단"이라 그 할인이 다 반영된 금액을 무엇으로 낼지 나중에 정한다.
        // (관람권을 먼저 빼버리면 %쿠폰이 줄어든 금액 기준으로 계산돼 원래 받아야 할 할인보다 적게 받는 문제가 있었다.)
        int discountRate = userService.discountRateFor(user.getGrade());
        boolean gradeDiscountEligible = discountRate > 0 && !reservationService.hasUsedGradeDiscountThisMonth(userId);
        if (request.isUseGradeDiscount() && !gradeDiscountEligible) {
            throw new IllegalStateException("등급 할인을 사용할 수 없습니다.");
        }
        int gradeDiscountAmount = (request.isUseGradeDiscount() && gradeDiscountEligible) ? totalPrice * discountRate / 100 : 0;
        int amountAfterGradeDiscount = totalPrice - gradeDiscountAmount;

        int couponDiscountAmount = 0;
        Long userCouponId = request.getUserCouponId();
        if (userCouponId != null) {
            UserCoupon userCoupon = userCouponService.findOwned(userCouponId, userId);
            couponDiscountAmount = userCouponService.previewDiscount(userCoupon, amountAfterGradeDiscount, CouponApplicableContext.RESERVATION);
        }
        int amountAfterDiscount = amountAfterGradeDiscount - couponDiscountAmount;

        // 관람권/기프티콘 + 포인트를 다 합쳐도 최소 결제금액(MIN_PAYABLE_AMOUNT)은 남겨야
        // Toss 위젯의 0원 결제라는 별도 케이스를 피할 수 있다.
        int voucherDiscountAmount = 0;
        Long ticketVoucherId = request.getTicketVoucherId();
        if (ticketVoucherId != null) {
            TicketVoucher voucher = ticketVoucherService.findOwned(ticketVoucherId, userId);
            int maxVoucherAmount = Math.max(0, amountAfterDiscount - MIN_PAYABLE_AMOUNT);
            voucherDiscountAmount = ticketVoucherService.previewDiscount(voucher, maxVoucherAmount);
        }
        int amountAfterVoucher = amountAfterDiscount - voucherDiscountAmount;

        int maxUsablePoint = Math.max(0, Math.min(user.getPoint(), amountAfterVoucher - MIN_PAYABLE_AMOUNT));
        if (request.getUsePoint() > maxUsablePoint) {
            throw new IllegalStateException("사용 가능한 포인트를 초과했습니다.");
        }
        int usedPoint = request.getUsePoint();
        int amount = amountAfterVoucher - usedPoint;

        String orderId = "movie24-" + UUID.randomUUID();

        session.setAttribute(SESSION_KEY, new PendingPayment(orderId, request.getShowtimeId(), request.getSeatIds(),
                amount, totalPrice, usedPoint, gradeDiscountAmount, userCouponId, couponDiscountAmount,
                ticketVoucherId, voucherDiscountAmount));

        return PaymentPrepareResponse.builder()
                .orderId(orderId)
                .orderName(buildOrderName(showtime, seats.size()))
                .amount(amount)
                .totalPrice(totalPrice)
                .usedPoint(usedPoint)
                .gradeDiscountAmount(gradeDiscountAmount)
                .couponDiscountAmount(couponDiscountAmount)
                .voucherDiscountAmount(voucherDiscountAmount)
                .build();
    }

    @Transactional(readOnly = true)
    public PendingPayment peekPending(HttpSession session, String orderId) {
        PendingPayment pending = (PendingPayment) session.getAttribute(SESSION_KEY);
        return (pending != null && pending.getOrderId().equals(orderId)) ? pending : null;
    }

    public Reservation confirmAndCreateReservation(HttpSession session, Long userId, String paymentKey, String orderId) {
        PendingPayment pending = peekPending(session, orderId);
        if (pending == null) {
            throw new IllegalStateException("결제 정보를 찾을 수 없습니다. 처음부터 다시 시도해주세요.");
        }

        // 결제 승인 시 우리가 서버에서 계산해 둔 금액(pending.amount)을 보낸다.
        // 클라이언트가 위젯 호출 전에 금액을 조작했다면, 토스에 실제 승인된 금액과 달라 여기서 거절된다.
        tossPaymentClient.confirm(paymentKey, orderId, pending.getAmount());

        try {
            Reservation reservation = reservationService.reserve(userId, pending.getShowtimeId(), pending.getSeatIds(),
                    pending.getUsedPoint(), pending.getGradeDiscountAmount(),
                    pending.getUserCouponId(), pending.getCouponDiscountAmount(),
                    pending.getTicketVoucherId(), pending.getVoucherDiscountAmount());
            session.removeAttribute(SESSION_KEY);
            return reservation;
        } catch (RuntimeException e) {
            // 결제는 승인됐는데 좌석 확보(중복 등)에 실패한 경우, 결제를 취소해 과금을 되돌린다.
            tossPaymentClient.cancel(paymentKey, "좌석 확보 실패로 인한 자동 취소");
            session.removeAttribute(SESSION_KEY);
            throw e;
        }
    }

    private String buildOrderName(Showtime showtime, int seatCount) {
        String title = showtime.getMovie().getTitle();
        return seatCount > 1 ? title + " 외 " + (seatCount - 1) + "매" : title;
    }
}
