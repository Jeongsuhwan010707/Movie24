package project.movie24.reservation.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.List;

@Getter
public class ReservationRequest {

    @NotNull
    private Long showtimeId;

    @NotEmpty
    private List<Long> seatIds;
}
