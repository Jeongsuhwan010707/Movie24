package project.movie24.point.domain;

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
import lombok.NoArgsConstructor;
import project.movie24.user.domain.User;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class PointHistory {

    @Id @GeneratedValue
    @Column(name = "point_history_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // Reservation 엔티티와 직접 연관관계를 맺지 않고 id만 저장해, point 패키지가 reservation에 의존하지 않게 한다.
    private Long reservationId;

    @Enumerated(EnumType.STRING)
    private PointHistoryType type;

    private Integer changeAmount;
    private Integer balanceAfter;
    private LocalDateTime createdAt;
}
