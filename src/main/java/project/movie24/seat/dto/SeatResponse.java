package project.movie24.seat.dto;

import lombok.Builder;
import lombok.Getter;
import project.movie24.seat.domain.Seat;

@Getter
@Builder
public class SeatResponse {

    private Long id;
    private Long screenId;
    private String screenName;
    private String rowLabel;
    private Integer seatNumber;
    private String seatLabel;

    public static SeatResponse from(Seat seat) {
        return SeatResponse.builder()
                .id(seat.getId())
                .screenId(seat.getScreen().getId())
                .screenName(seat.getScreen().getName())
                .rowLabel(seat.getRowLabel())
                .seatNumber(seat.getSeatNumber())
                .seatLabel(seat.getSeatLabel())
                .build();
    }
}
