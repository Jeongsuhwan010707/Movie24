package project.movie24.event.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import project.movie24.event.domain.Event;
import project.movie24.event.domain.EventStatus;
import project.movie24.event.dto.EventRequest;
import project.movie24.event.dto.EventResponse;
import project.movie24.event.service.EventService;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventApiController {

    private final EventService eventService;

    @PostMapping
    public ResponseEntity<EventResponse> register(@Valid @RequestBody EventRequest request) {
        Event event = eventService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(EventResponse.from(event));
    }

    @GetMapping
    public ResponseEntity<List<EventResponse>> list(@RequestParam(required = false) EventStatus status) {
        List<Event> events = status == null ? eventService.findAll() : eventService.findByStatus(status);
        return ResponseEntity.ok(events.stream().map(EventResponse::from).toList());
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventResponse> findOne(@PathVariable Long eventId) {
        return ResponseEntity.ok(EventResponse.from(eventService.findOne(eventId)));
    }

    @PutMapping("/{eventId}")
    public ResponseEntity<EventResponse> update(@PathVariable Long eventId, @Valid @RequestBody EventRequest request) {
        Event event = eventService.update(eventId, request);
        return ResponseEntity.ok(EventResponse.from(event));
    }

    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> delete(@PathVariable Long eventId) {
        eventService.delete(eventId);
        return ResponseEntity.noContent().build();
    }
}
