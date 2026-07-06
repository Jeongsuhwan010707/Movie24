package project.movie24.movie.dto;

import lombok.Builder;
import lombok.Getter;
import project.movie24.movie.domain.AgeRating;
import project.movie24.movie.domain.Movie;
import project.movie24.movie.domain.ScreeningStatus;

import java.time.LocalDate;

@Getter
@Builder
public class MovieResponse {

    private Long id;
    private String title;
    private String director;
    private String actors;
    private String genre;
    private AgeRating ageRating;
    private Integer runtimeMinutes;
    private String country;
    private LocalDate releaseDate;
    private String synopsis;
    private String posterImageUrl;
    private String trailerVideoUrl;
    private ScreeningStatus status;

    public static MovieResponse from(Movie movie) {
        return MovieResponse.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .director(movie.getDirector())
                .actors(movie.getActors())
                .genre(movie.getGenre())
                .ageRating(movie.getAgeRating())
                .runtimeMinutes(movie.getRuntimeMinutes())
                .country(movie.getCountry())
                .releaseDate(movie.getReleaseDate())
                .synopsis(movie.getSynopsis())
                .posterImageUrl(movie.getPosterImageUrl())
                .trailerVideoUrl(movie.getTrailerVideoUrl())
                .status(movie.getStatus())
                .build();
    }
}
