package project.movie24.seat.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import project.movie24.screen.domain.Screen;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"screen_id", "row_label", "seat_number"}))
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
public class Seat {

    @Id @GeneratedValue
    @Column(name = "seat_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "screen_id")
    private Screen screen;

    private String rowLabel;
    private Integer seatNumber;

    public Seat(){}

    public String getSeatLabel() {
        return rowLabel + seatNumber;
    }
}
