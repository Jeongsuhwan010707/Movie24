package project.movie24.reservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.movie24.reservation.domain.ReservationSeat;
import project.movie24.reservation.domain.ReservationStatus;

import java.util.List;

public interface ReservationSeatRepository extends JpaRepository<ReservationSeat, Long> {

    List<ReservationSeat> findByReservationId(Long reservationId);

    List<ReservationSeat> findByShowtimeIdAndReservation_Status(Long showtimeId, ReservationStatus status);
}
