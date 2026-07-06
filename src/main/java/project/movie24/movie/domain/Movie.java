package project.movie24.movie.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Entity
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
public class Movie {

    @Id @GeneratedValue
    @Column(name = "movie_id")
    private Long id;

    private String title;
    private String director;
    private String actors;
    private String genre;

    @Enumerated(EnumType.STRING)
    private AgeRating ageRating;

    private Integer runtimeMinutes;
    private String country;
    private LocalDate releaseDate;

    @Column(columnDefinition = "TEXT")
    private String synopsis;

    private String posterImageUrl;
    private String trailerVideoUrl;

    @Enumerated(EnumType.STRING)
    private ScreeningStatus status;

    public Movie(){}

    public void update(String title, String director, String actors, String genre,
                        AgeRating ageRating, Integer runtimeMinutes, String country,
                        LocalDate releaseDate, String synopsis, String posterImageUrl,
                        String trailerVideoUrl, ScreeningStatus status) {
        this.title = title;
        this.director = director;
        this.actors = actors;
        this.genre = genre;
        this.ageRating = ageRating;
        this.runtimeMinutes = runtimeMinutes;
        this.country = country;
        this.releaseDate = releaseDate;
        this.synopsis = synopsis;
        this.posterImageUrl = posterImageUrl;
        this.trailerVideoUrl = trailerVideoUrl;
        this.status = status;
    }
}
