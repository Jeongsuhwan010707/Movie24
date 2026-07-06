package project.movie24.reservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.movie24.reservation.domain.Reservation;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByUserId(Long userId);
}
