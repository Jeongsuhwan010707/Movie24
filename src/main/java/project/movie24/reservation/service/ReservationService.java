package project.movie24.reservation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.movie24.common.EntityFinders;
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
import project.movie24.user.domain.User;
import project.movie24.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationSeatRepository reservationSeatRepository;
    private final SeatService seatService;
    private final ShowtimeService showtimeService;
    private final UserRepository userRepository;

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

    public void cancel(Long reservationId, Long currentUserId) {
        Reservation reservation = getOrThrow(reservationId);
        if (!reservation.getUser().getId().equals(currentUserId)) {
            throw new IllegalStateException("본인의 예매만 취소할 수 있습니다.");
        }
        reservation.cancel();
        // 좌석 점유 기록 자체를 지워야 (showtime_id, seat_id) 유니크 제약에 다시 걸리지 않고
        // 같은 좌석을 재예매할 수 있다.
        reservationSeatRepository.deleteAll(reservationSeatRepository.findByReservationId(reservationId));
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
