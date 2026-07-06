package project.movie24.movie.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import project.movie24.movie.domain.AgeRating;
import project.movie24.movie.domain.ScreeningStatus;

import java.time.LocalDate;

@Getter
public class MovieRequest {

    @NotBlank
    private String title;

    private String director;
    private String actors;
    private String genre;

    @NotNull
    private AgeRating ageRating;

    @Positive
    private Integer runtimeMinutes;

    private String country;
    private LocalDate releaseDate;
    private String synopsis;
    private String posterImageUrl;
    private String trailerVideoUrl;

    @NotNull
    private ScreeningStatus status;
}
