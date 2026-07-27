package project.movie24.moviereservation.controller;

import lombok.AllArgsConstructor;
import lombok.Getter;
import project.movie24.movie.domain.Movie;

import java.util.List;

/**
 * time.html에서 극장별 모드일 때 영화별로 상영관 스케줄을 묶어 보여주기 위한 화면 전용 뷰 모델.
 */
@Getter
@AllArgsConstructor
public class MovieSchedule {
    private Movie movie;
    private List<ScreenSchedule> screens;
}
