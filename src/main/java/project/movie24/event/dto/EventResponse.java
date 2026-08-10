package project.movie24.event.dto;

import lombok.Builder;
import lombok.Getter;
import project.movie24.event.domain.Event;
import project.movie24.event.domain.EventStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class EventResponse {

    private Long id;
    private String title;
    private String content;
    private String thumbnailUrl;
    private EventStatus status;
    private Boolean winnerAnnounced;
    private String winnerContent;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer displayOrder;
    private LocalDateTime createdAt;

    public static EventResponse from(Event event) {
        return EventResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .content(event.getContent())
                .thumbnailUrl(event.getThumbnailUrl())
                .status(event.getStatus())
                .winnerAnnounced(event.getWinnerAnnounced())
                .winnerContent(event.getWinnerContent())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .displayOrder(event.getDisplayOrder())
                .createdAt(event.getCreatedAt())
                .build();
    }
}
