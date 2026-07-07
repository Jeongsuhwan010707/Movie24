package project.movie24.reservation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import project.movie24.showtime.domain.Showtime;
import project.movie24.user.domain.User;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
public class Reservation {

    @Id @GeneratedValue
    @Column(name = "reservation_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "showtime_id")
    private Showtime showtime;

    private Integer totalPrice;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    private LocalDateTime reservedAt;

    public Reservation(){}

    public void cancel() {
        this.status = ReservationStatus.CANCELLED;
    }
}
