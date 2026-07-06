package project.movie24.showtime.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ShowtimeRequest {

    @NotNull
    private Long movieId;

    @NotNull
    private Long screenId;

    @NotNull
    private LocalDateTime startTime;

    @Positive
    private Integer basePrice;
}
