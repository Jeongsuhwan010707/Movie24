package project.movie24.reservation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.movie24.reservation.domain.Reservation;
import project.movie24.reservation.domain.ReservationSeat;
import project.movie24.reservation.domain.ReservationStatus;
import project.movie24.reservation.dto.ReservationRequest;
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

@Service
@Transactional
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationSeatRepository reservationSeatRepository;
    private final SeatService seatService;
    private final ShowtimeService showtimeService;
    private final UserRepository userRepository;

    public Reservation reserve(Long userId, ReservationRequest request) {
        Showtime showtime = showtimeService.findOne(request.getShowtimeId());
        List<Seat> seats = seatService.findAllByIds(request.getSeatIds());
        if (seats.size() != request.getSeatIds().size()) {
            throw new IllegalArgumentException("존재하지 않는 좌석이 포함되어 있습니다.");
        }
        for (Seat seat : seats) {
            if (!seat.getScreen().getId().equals(showtime.getScreen().getId())) {
                throw new IllegalArgumentException("해당 상영관의 좌석이 아닙니다: " + seat.getSeatLabel());
            }
        }

        User user = userRepository.getReferenceById(userId);
        Reservation reservation = reservationRepository.save(Reservation.builder()
                .user(user)
                .showtime(showtime)
                .totalPrice(showtime.getBasePrice() * seats.size())
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
    }

    @Transactional(readOnly = true)
    public List<Reservation> findMyReservations(Long userId) {
        return reservationRepository.findByUserId(userId);
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

    private Reservation getOrThrow(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예매입니다. id=" + reservationId));
    }
}
