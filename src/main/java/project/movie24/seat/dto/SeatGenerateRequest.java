package project.movie24.seat.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

@Getter
public class SeatGenerateRequest {

    @NotNull
    private Long screenId;

    @Positive
    @Max(26)
    private Integer rowCount;

    @Positive
    private Integer seatsPerRow;
}
