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
import lombok.NoArgsConstructor;
import project.movie24.showtime.domain.Showtime;
import project.movie24.user.domain.User;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
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

    @Builder.Default
    private int earnedPoint = 0;

    @Builder.Default
    private int usedPoint = 0;

    @Builder.Default
    private int gradeDiscountAmount = 0;

    @Builder.Default
    private int couponDiscountAmount = 0;

    // UserCoupon 엔티티와 직접 연관관계를 맺지 않고 id만 저장해, reservation 패키지가 coupon에 의존하지 않게 한다.
    private Long userCouponId;

    @Builder.Default
    private int voucherDiscountAmount = 0;

    // TicketVoucher 엔티티와 직접 연관관계를 맺지 않고 id만 저장해, reservation 패키지가 store에 의존하지 않게 한다.
    private Long ticketVoucherId;

    public void cancel() {
        this.status = ReservationStatus.CANCELLED;
    }
}
