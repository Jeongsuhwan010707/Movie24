package project.movie24.screen.domain;

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
import project.movie24.theater.domain.Theater;

@Entity
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
public class Screen {

    @Id @GeneratedValue
    @Column(name = "screen_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theater_id")
    private Theater theater;

    private String name;
    private Integer totalSeats;
    private String screenType;

    public Screen(){}

    public void update(Theater theater, String name, Integer totalSeats, String screenType) {
        this.theater = theater;
        this.name = name;
        this.totalSeats = totalSeats;
        this.screenType = screenType;
    }
}
