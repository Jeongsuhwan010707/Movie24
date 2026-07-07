package project.movie24.theater.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.movie24.theater.domain.Theater;

import java.util.List;

public interface TheaterRepository extends JpaRepository<Theater, Long> {

    List<Theater> findByRegion(String region);
}
