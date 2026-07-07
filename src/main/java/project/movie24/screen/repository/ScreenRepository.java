package project.movie24.screen.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.movie24.screen.domain.Screen;

import java.util.List;

public interface ScreenRepository extends JpaRepository<Screen, Long> {

    List<Screen> findByTheaterId(Long theaterId);
}
