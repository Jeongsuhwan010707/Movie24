package project.movie24.seat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

@Getter
public class SeatRequest {

    @NotNull
    private Long screenId;

    @NotBlank
    private String rowLabel;

    @Positive
    private Integer seatNumber;
}
