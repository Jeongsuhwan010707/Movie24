package project.movie24.moviereservation.controller;

import lombok.AllArgsConstructor;
import lombok.Getter;
import project.movie24.showtime.domain.Showtime;

import java.time.LocalDateTime;

/**
 * time.html에서 상영시간표 옆에 종료 시각·잔여 좌석 수를 함께 보여주기 위한 화면 전용 뷰 모델.
 */
@Getter
@AllArgsConstructor
public class ShowtimeSchedule {
    private Showtime showtime;
    private LocalDateTime endTime;
    private int remainingSeats;
}
