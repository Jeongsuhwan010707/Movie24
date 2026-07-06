package project.movie24.showtime.dto;

import lombok.Builder;
import lombok.Getter;
import project.movie24.showtime.domain.Showtime;

import java.time.LocalDateTime;

@Getter
@Builder
public class ShowtimeResponse {

    private Long id;
    private Long movieId;
    private String movieTitle;
    private Long screenId;
    private String screenName;
    private Long theaterId;
    private String theaterName;
    private LocalDateTime startTime;
    private Integer basePrice;

    public static ShowtimeResponse from(Showtime showtime) {
        return ShowtimeResponse.builder()
                .id(showtime.getId())
                .movieId(showtime.getMovie().getId())
                .movieTitle(showtime.getMovie().getTitle())
                .screenId(showtime.getScreen().getId())
                .screenName(showtime.getScreen().getName())
                .theaterId(showtime.getScreen().getTheater().getId())
                .theaterName(showtime.getScreen().getTheater().getName())
                .startTime(showtime.getStartTime())
                .basePrice(showtime.getBasePrice())
                .build();
    }
}
