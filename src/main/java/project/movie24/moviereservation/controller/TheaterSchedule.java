package project.movie24.moviereservation.controller;

import lombok.AllArgsConstructor;
import lombok.Getter;
import project.movie24.theater.domain.Theater;

import java.util.List;

/**
 * time.html에서 극장별로 상영관 스케줄을 묶어 보여주기 위한 화면 전용 뷰 모델.
 */
@Getter
@AllArgsConstructor
public class TheaterSchedule {
    private Theater theater;
    private List<ScreenSchedule> screens;
}
