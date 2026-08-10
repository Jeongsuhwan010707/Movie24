package project.movie24.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.movie24.event.domain.Event;
import project.movie24.event.domain.EventStatus;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByStatus(EventStatus status);

    List<Event> findByWinnerAnnouncedTrue();
}
