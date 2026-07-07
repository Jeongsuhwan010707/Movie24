package project.movie24.moviereservation.controller;

import lombok.AllArgsConstructor;
import lombok.Getter;
import project.movie24.screen.domain.Screen;
import project.movie24.showtime.domain.Showtime;

import java.util.List;

/**
 * time.html에서 상영관별로 상영시간표를 묶어 보여주기 위한 화면 전용 뷰 모델.
 */
@Getter
@AllArgsConstructor
public class ScreenSchedule {
    private Screen screen;
    private List<Showtime> showtimes;
}
