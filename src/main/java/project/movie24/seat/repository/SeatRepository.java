package project.movie24.seat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.movie24.seat.domain.Seat;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByScreenId(Long screenId);
}
