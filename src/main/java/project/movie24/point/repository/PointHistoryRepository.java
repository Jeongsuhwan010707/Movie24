package project.movie24.point.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.movie24.point.domain.PointHistory;

import java.util.List;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {

    List<PointHistory> findByUserIdOrderByCreatedAtDesc(Long userId);
}
