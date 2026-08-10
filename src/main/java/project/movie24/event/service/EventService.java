package project.movie24.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.movie24.common.EntityFinders;
import project.movie24.event.domain.Event;
import project.movie24.event.domain.EventStatus;
import project.movie24.event.dto.EventRequest;
import project.movie24.event.repository.EventRepository;

import java.util.Comparator;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    public Event register(EventRequest request) {
        Event event = Event.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .thumbnailUrl(request.getThumbnailUrl())
                .status(request.getStatus())
                .winnerAnnounced(request.getWinnerAnnounced() != null ? request.getWinnerAnnounced() : false)
                .winnerContent(request.getWinnerContent())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .build();
        return eventRepository.save(event);
    }

    public Event update(Long eventId, EventRequest request) {
        Event event = getOrThrow(eventId);
        event.update(request.getTitle(), request.getContent(), request.getThumbnailUrl(), request.getStatus(),
                request.getWinnerAnnounced(), request.getWinnerContent(),
                request.getStartDate(), request.getEndDate(), request.getDisplayOrder());
        return event;
    }

    public void delete(Long eventId) {
        eventRepository.delete(getOrThrow(eventId));
    }

    @Transactional(readOnly = true)
    public Event findOne(Long eventId) {
        return getOrThrow(eventId);
    }

    @Transactional(readOnly = true)
    public List<Event> findAll() {
        return eventRepository.findAll().stream()
                .sorted(Comparator.comparing(Event::getDisplayOrder))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Event> findByStatus(EventStatus status) {
        return eventRepository.findByStatus(status).stream()
                .sorted(Comparator.comparing(Event::getDisplayOrder))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Event> findWinnerAnnounced() {
        return eventRepository.findByWinnerAnnouncedTrue().stream()
                .sorted(Comparator.comparing(Event::getDisplayOrder))
                .toList();
    }

    private Event getOrThrow(Long eventId) {
        return EntityFinders.getOrThrow(eventRepository, eventId, "이벤트");
    }
}
