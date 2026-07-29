package project.movie24.payment.service;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final String SESSION_KEY = "PENDING_PAYMENT";

    private final ShowtimeService showtimeService;
    private final SeatService seatService;
    private final ReservationService reservationService;
    private final TossPaymentClient tossPaymentClient;

    @Transactional(readOnly = true)
    public PaymentPrepareResponse prepare(HttpSession session, PaymentPrepareRequest request) {
        Showtime showtime = showtimeService.findOne(request.getShowtimeId());
        List<Seat> seats = seatService.findAllByIds(request.getSeatIds());
        if (seats.size() != request.getSeatIds().size()) {
            throw new IllegalArgumentException("존재하지 않는 좌석이 포함되어 있습니다.");
        }

        int amount = showtime.getBasePrice() * seats.size();
        String orderId = "movie24-" + UUID.randomUUID();

        session.setAttribute(SESSION_KEY, new PendingPayment(orderId, request.getShowtimeId(), request.getSeatIds(), amount));

        return PaymentPrepareResponse.builder()
                .orderId(orderId)
                .orderName(buildOrderName(showtime, seats.size()))
                .amount(amount)
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
            Reservation reservation = reservationService.reserve(userId, pending.getShowtimeId(), pending.getSeatIds());
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
