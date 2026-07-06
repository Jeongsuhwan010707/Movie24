package project.movie24.reservation.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class SeatAvailabilityResponse {

    private Long showtimeId;
    private List<Long> reservedSeatIds;
}
