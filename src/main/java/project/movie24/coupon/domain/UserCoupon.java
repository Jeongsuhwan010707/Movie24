package project.movie24.coupon.domain;

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
public class UserCoupon {

    @Id @GeneratedValue
    @Column(name = "user_coupon_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id")
    private Coupon coupon;

    @Enumerated(EnumType.STRING)
    private UserCouponStatus status;

    private LocalDateTime issuedAt;
    private LocalDateTime expiresAt;
    private LocalDateTime usedAt;

    // Reservation 엔티티와 직접 연관관계를 맺지 않고 id만 저장해, coupon 패키지가 reservation에 의존하지 않게 한다.
    private Long reservationId;

    private Integer discountAmountApplied;

    public void markUsed(LocalDateTime usedAt, Long reservationId, int discountAmountApplied) {
        this.status = UserCouponStatus.USED;
        this.usedAt = usedAt;
        this.reservationId = reservationId;
        this.discountAmountApplied = discountAmountApplied;
    }

    public void markUnused() {
        this.status = UserCouponStatus.UNUSED;
        this.usedAt = null;
        this.reservationId = null;
        this.discountAmountApplied = null;
    }

    public boolean isExpired(LocalDateTime now) {
        return expiresAt != null && expiresAt.isBefore(now);
    }
}
