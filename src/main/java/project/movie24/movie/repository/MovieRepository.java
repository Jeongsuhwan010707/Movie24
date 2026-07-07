package project.movie24.movie.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.movie24.movie.domain.Movie;
import project.movie24.movie.domain.ScreeningStatus;

import java.util.List;

public interface MovieRepository extends JpaRepository<Movie, Long> {

    List<Movie> findByStatus(ScreeningStatus status);
}
