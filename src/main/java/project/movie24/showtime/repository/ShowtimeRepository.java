package project.movie24.showtime.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.movie24.showtime.domain.Showtime;

import java.time.LocalDateTime;
import java.util.List;

public interface ShowtimeRepository extends JpaRepository<Showtime, Long> {

    List<Showtime> findByMovieId(Long movieId);

    List<Showtime> findByScreenId(Long screenId);

    List<Showtime> findByMovieIdAndStartTimeBetween(Long movieId, LocalDateTime start, LocalDateTime end);
}
