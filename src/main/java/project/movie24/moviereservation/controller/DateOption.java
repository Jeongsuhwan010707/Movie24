package project.movie24.moviereservation.controller;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

/**
 * time.html의 날짜 스크롤러(예: "10토")를 그리기 위한 화면 전용 뷰 모델.
 */
@Getter
@AllArgsConstructor
public class DateOption {
    private LocalDate date;
    private String dayLabel;
    private String weekdayLabel;
    private String weekendClass;
}
