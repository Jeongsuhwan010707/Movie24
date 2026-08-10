package project.movie24.event.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import project.movie24.event.domain.Event;
import project.movie24.event.domain.EventStatus;
import project.movie24.event.service.EventService;

import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private static final int PAGE_SIZE = 8;

    private final EventService eventService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "ongoing") String tab,
                        @RequestParam(defaultValue = "1") int page,
                        Model model) {
        List<Event> events = switch (tab) {
            case "ended" -> eventService.findByStatus(EventStatus.ENDED);
            case "winner" -> eventService.findWinnerAnnounced();
            default -> eventService.findByStatus(EventStatus.ONGOING);
        };

        List<Event> sorted = events.stream()
                .sorted(Comparator.comparing(Event::getDisplayOrder))
                .toList();

        int totalPages = Math.max(1, (int) Math.ceil(sorted.size() / (double) PAGE_SIZE));
        int currentPage = Math.min(Math.max(page, 1), totalPages);
        List<Event> pageEvents = sorted.stream()
                .skip((long) (currentPage - 1) * PAGE_SIZE)
                .limit(PAGE_SIZE)
                .toList();

        model.addAttribute("events", pageEvents);
        model.addAttribute("selectedTab", tab);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        return "event/list";
    }

    @GetMapping("/{eventId}")
    public String detail(@PathVariable Long eventId, Model model) {
        Event event;
        try {
            event = eventService.findOne(eventId);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
        model.addAttribute("event", event);
        return "event/detail";
    }
}
