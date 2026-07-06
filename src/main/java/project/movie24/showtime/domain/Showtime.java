package project.movie24.showtime.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import project.movie24.movie.domain.Movie;
import project.movie24.screen.domain.Screen;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
public class Showtime {

    @Id @GeneratedValue
    @Column(name = "showtime_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id")
    private Movie movie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "screen_id")
    private Screen screen;

    private LocalDateTime startTime;
    private Integer basePrice;

    public Showtime(){}

    public void update(Movie movie, Screen screen, LocalDateTime startTime, Integer basePrice) {
        this.movie = movie;
        this.screen = screen;
        this.startTime = startTime;
        this.basePrice = basePrice;
    }
}
