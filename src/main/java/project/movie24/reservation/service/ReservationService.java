package project.movie24.reservation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.movie24.common.EntityFinders;
import project.movie24.coupon.service.UserCouponService;
import project.movie24.point.service.PointService;
import project.movie24.reservation.domain.Reservation;
import project.movie24.reservation.domain.ReservationSeat;
import project.movie24.reservation.domain.ReservationStatus;
import project.movie24.reservation.dto.ReservationResponse;
import project.movie24.reservation.repository.ReservationRepository;
import project.movie24.reservation.repository.ReservationSeatRepository;
import project.movie24.seat.domain.Seat;
import project.movie24.seat.service.SeatService;
import project.movie24.showtime.domain.Showtime;
import project.movie24.showtime.service.ShowtimeService;
import project.movie24.store.service.TicketVoucherService;
import project.movie24.user.domain.User;
import project.movie24.user.repository.UserRepository;
import project.movie24.user.service.UserService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ReservationService {

    // 결제(현금)로 낸 금액에 대해 적립되는 포인트 비율.
    private static final double POINT_EARN_RATE = 0.02;

    private final ReservationRepository reservationRepository;
    private final ReservationSeatRepository reservationSeatRepository;
    private final SeatService seatService;
    private final ShowtimeService showtimeService;
    private final UserRepository userRepository;
    private final PointService pointService;
    private final UserService userService;
    private final UserCouponService userCouponService;
    private final TicketVoucherService ticketVoucherService;

    public Reservation reserve(Long userId, Long showtimeId, List<Long> seatIds) {
        Showtime showtime = showtimeService.findOne(showtimeId);
        List<Seat> seats = seatService.findAllByIdsOrThrow(seatIds);
        for (Seat seat : seats) {
            if (!seat.getScreen().getId().equals(showtime.getScreen().getId())) {
                throw new IllegalArgumentException("해당 상영관의 좌석이 아닙니다: " + seat.getSeatLabel());
            }
        }

        User user = userRepository.getReferenceById(userId);
        Reservation reservation = reservationRepository.save(Reservation.builder()
                .user(user)
                .showtime(showtime)
                .totalPrice(showtime.priceFor(seats.size()))
                .status(ReservationStatus.RESERVED)
                .reservedAt(LocalDateTime.now())
                .build());

        // 좌석마다 즉시 flush해서, 동시에 같은 좌석을 예매하려는 다른 요청이 있으면
        // (showtime_id, seat_id) 유니크 제약 위반으로 바로 걸러낸다.
        for (Seat seat : seats) {
            try {
                reservationSeatRepository.saveAndFlush(ReservationSeat.builder()
                        .reservation(reservation)
                        .seat(seat)
                        .showtime(showtime)
                        .build());
            } catch (DataIntegrityViolationException e) {
                throw new IllegalStateException("이미 예약된 좌석입니다: " + seat.getSeatLabel());
            }
        }

        return reservation;
    }

    /**
     * 결제 흐름 전용: 좌석 확보에 이어 포인트 사용/적립과 등급 재계산까지 한 트랜잭션 안에서 처리한다.
     * 좌석 확보와 트랜잭션을 분리하면 포인트 차감 실패 시 이미 커밋된 예매가 롤백되지 않으므로 반드시 같은 메서드에서 처리한다.
     */
    public Reservation reserve(Long userId, Long showtimeId, List<Long> seatIds, int usePoint, int gradeDiscountAmount) {
        return reserve(userId, showtimeId, seatIds, usePoint, gradeDiscountAmount, null, 0, null, 0);
    }

    /**
     * 결제 흐름 전용(쿠폰 포함): 좌석 확보에 이어 쿠폰/포인트 사용/적립과 등급 재계산까지 한 트랜잭션 안에서 처리한다.
     */
    public Reservation reserve(Long userId, Long showtimeId, List<Long> seatIds, int usePoint, int gradeDiscountAmount,
                                Long userCouponId, int couponDiscountAmount) {
        return reserve(userId, showtimeId, seatIds, usePoint, gradeDiscountAmount, userCouponId, couponDiscountAmount, null, 0);
    }

    /**
     * 결제 흐름 전용(관람권/기프티콘 포함): 좌석 확보에 이어 관람권/쿠폰/포인트 사용·적립과 등급 재계산까지
     * 한 트랜잭션 안에서 처리한다.
     */
    public Reservation reserve(Long userId, Long showtimeId, List<Long> seatIds, int usePoint, int gradeDiscountAmount,
                                Long userCouponId, int couponDiscountAmount,
                                Long ticketVoucherId, int voucherDiscountAmount) {
        Reservation reservation = reserve(userId, showtimeId, seatIds);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        if (ticketVoucherId != null) {
            ticketVoucherService.markUsed(ticketVoucherId, userId, reservation.getId());
        }

        if (userCouponId != null) {
            userCouponService.markUsed(userCouponId, userId, couponDiscountAmount, reservation.getId());
        }

        if (usePoint > 0) {
            user = pointService.use(user, usePoint, reservation.getId());
        }

        int cashPaid = reservation.getTotalPrice() - voucherDiscountAmount - gradeDiscountAmount - couponDiscountAmount - usePoint;
        int earnedPoint = (int) Math.floor(cashPaid * POINT_EARN_RATE);
        pointService.earn(user, earnedPoint, reservation.getId());

        reservation = reservationRepository.save(reservation.toBuilder()
                .earnedPoint(earnedPoint)
                .usedPoint(usePoint)
                .gradeDiscountAmount(gradeDiscountAmount)
                .couponDiscountAmount(couponDiscountAmount)
                .userCouponId(userCouponId)
                .voucherDiscountAmount(voucherDiscountAmount)
                .ticketVoucherId(ticketVoucherId)
                .build());

        userService.recalculateGrade(userId, sumPaidAmountLastYear(userId));

        return reservation;
    }

    /**
     * VIP 등급 판단 기준인 최근 1년 누적 결제금액. 마이페이지의 "다음 등급까지" 표시에도 쓰인다.
     */
    @Transactional(readOnly = true)
    public long sumPaidAmountLastYear(Long userId) {
        return reservationRepository.sumPaidAmountSince(userId, ReservationStatus.RESERVED, LocalDateTime.now().minusYears(1));
    }

    /**
     * 등급 할인은 달력 월 기준 1회만 허용한다. 취소된 예매는 대상에서 빠지므로,
     * 할인을 쓴 예매를 취소하면 그 달에 다시 쓸 수 있다.
     */
    @Transactional(readOnly = true)
    public boolean hasUsedGradeDiscountThisMonth(Long userId) {
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        return reservationRepository.existsByUser_IdAndGradeDiscountAmountGreaterThanAndStatusAndReservedAtAfter(
                userId, 0, ReservationStatus.RESERVED, monthStart);
    }

    public void cancel(Long reservationId, Long currentUserId) {
        Reservation reservation = getOrThrow(reservationId);
        if (!reservation.getUser().getId().equals(currentUserId)) {
            throw new IllegalStateException("본인의 예매만 취소할 수 있습니다.");
        }
        reservation.cancel();
        // 좌석 점유 기록 자체를 지워야 (showtime_id, seat_id) 유니크 제약에 다시 걸리지 않고
        // 같은 좌석을 재예매할 수 있다.
        reservationSeatRepository.deleteAll(reservationSeatRepository.findByReservationId(reservationId));

        if (reservation.getUsedPoint() > 0 || reservation.getEarnedPoint() > 0) {
            User user = userRepository.findById(currentUserId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
            if (reservation.getUsedPoint() > 0) {
                user = pointService.cancelUse(user, reservation.getUsedPoint(), reservationId);
            }
            if (reservation.getEarnedPoint() > 0) {
                pointService.cancelEarn(user, reservation.getEarnedPoint(), reservationId);
            }
        }

        if (reservation.getCouponDiscountAmount() > 0) {
            userCouponService.cancelUse(reservationId);
        }

        if (reservation.getVoucherDiscountAmount() > 0) {
            ticketVoucherService.cancelUse(reservationId);
        }
    }

    /**
     * 예매 건마다 상영시간/영화/상영관/극장/좌석을 따로 조회하지 않도록, 두 번의 쿼리(예매+상세 조인,
     * 좌석 일괄 조회)로 마이페이지/예매내역 목록에 필요한 응답을 한 번에 만든다.
     */
    @Transactional(readOnly = true)
    public List<ReservationResponse> findMyReservations(Long userId) {
        List<Reservation> reservations = reservationRepository.findByUserIdWithDetails(userId);
        List<Long> reservationIds = reservations.stream().map(Reservation::getId).toList();

        Map<Long, List<String>> seatLabelsByReservationId = reservationSeatRepository.findByReservationIdIn(reservationIds).stream()
                .collect(Collectors.groupingBy(rs -> rs.getReservation().getId(),
                        Collectors.mapping(rs -> rs.getSeat().getSeatLabel(), Collectors.toList())));

        return reservations.stream()
                .map(r -> ReservationResponse.from(r, seatLabelsByReservationId.getOrDefault(r.getId(), List.of())))
                .toList();
    }

    @Transactional(readOnly = true)
    public Reservation findOwned(Long reservationId, Long userId) {
        Reservation reservation = getOrThrow(reservationId);
        if (!reservation.getUser().getId().equals(userId)) {
            throw new IllegalStateException("본인의 예매만 조회할 수 있습니다.");
        }
        return reservation;
    }

    @Transactional(readOnly = true)
    public List<String> findSeatLabels(Long reservationId) {
        return reservationSeatRepository.findByReservationId(reservationId).stream()
                .map(rs -> rs.getSeat().getSeatLabel())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Long> findReservedSeatIds(Long showtimeId) {
        return reservationSeatRepository.findByShowtimeIdAndReservation_Status(showtimeId, ReservationStatus.RESERVED).stream()
                .map(rs -> rs.getSeat().getId())
                .toList();
    }

    /**
     * 상영시간표 화면에서 잔여 좌석을 계산할 때, 상영시간마다 따로 쿼리하지 않도록
     * 여러 상영시간의 예약 좌석 수를 한 번에 조회한다.
     */
    @Transactional(readOnly = true)
    public Map<Long, Integer> countReservedSeats(List<Long> showtimeIds) {
        if (showtimeIds.isEmpty()) {
            return Map.of();
        }
        return reservationSeatRepository.countReservedSeatsByShowtimeIdIn(showtimeIds, ReservationStatus.RESERVED).stream()
                .collect(Collectors.toMap(
                        ReservationSeatRepository.ShowtimeReservedCount::getShowtimeId,
                        c -> c.getReservedCount().intValue()));
    }

    private Reservation getOrThrow(Long reservationId) {
        return EntityFinders.getOrThrow(reservationRepository, reservationId, "예매");
    }
}
