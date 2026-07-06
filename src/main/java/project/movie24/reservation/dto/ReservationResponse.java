package project.movie24.reservation.dto;

import lombok.Builder;
import lombok.Getter;
import project.movie24.reservation.domain.Reservation;
import project.movie24.reservation.domain.ReservationStatus;
import project.movie24.showtime.domain.Showtime;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ReservationResponse {

    private Long id;
    private Long showtimeId;
    private String movieTitle;
    private String theaterName;
    private String screenName;
    private LocalDateTime startTime;
    private List<String> seatLabels;
    private Integer totalPrice;
    private ReservationStatus status;
    private LocalDateTime reservedAt;

    public static ReservationResponse from(Reservation reservation, List<String> seatLabels) {
        Showtime showtime = reservation.getShowtime();
        return ReservationResponse.builder()
                .id(reservation.getId())
                .showtimeId(showtime.getId())
                .movieTitle(showtime.getMovie().getTitle())
                .theaterName(showtime.getScreen().getTheater().getName())
                .screenName(showtime.getScreen().getName())
                .startTime(showtime.getStartTime())
                .seatLabels(seatLabels)
                .totalPrice(reservation.getTotalPrice())
                .status(reservation.getStatus())
                .reservedAt(reservation.getReservedAt())
                .build();
    }
}
