package project.movie24.coupon.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class Coupon {

    @Id @GeneratedValue
    @Column(name = "coupon_id")
    private Long id;

    private String name;
    private String description;

    @Enumerated(EnumType.STRING)
    private DiscountType discountType;

    private Integer discountValue;

    // PERCENT 타입에서만 관례적으로 사용하는 상한액. AMOUNT 타입은 무시한다.
    private Integer maxDiscountAmount;

    @Builder.Default
    private Integer minPurchaseAmount = 0;

    @Enumerated(EnumType.STRING)
    private CouponApplicableContext applicableContext;

    // 관리자 배포용 등록 코드. 이벤트 연동형(eventId)과는 실질적으로 배타적으로 사용한다.
    private String code;

    // Event 엔티티와 직접 연관관계를 맺지 않고 id만 저장해, coupon 패키지가 event에 의존하지 않게 한다.
    private Long eventId;

    private Integer validDays;
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;

    private Integer totalQuantity;

    @Builder.Default
    private int issuedQuantity = 0;

    @Builder.Default
    private boolean active = true;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public void update(String name, String description, DiscountType discountType, Integer discountValue,
                        Integer maxDiscountAmount, Integer minPurchaseAmount, CouponApplicableContext applicableContext,
                        String code, Long eventId, Integer validDays, LocalDateTime validFrom, LocalDateTime validUntil,
                        Integer totalQuantity, Boolean active) {
        this.name = name;
        this.description = description;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.maxDiscountAmount = maxDiscountAmount;
        this.minPurchaseAmount = minPurchaseAmount != null ? minPurchaseAmount : 0;
        this.applicableContext = applicableContext;
        this.code = code;
        this.eventId = eventId;
        this.validDays = validDays;
        this.validFrom = validFrom;
        this.validUntil = validUntil;
        this.totalQuantity = totalQuantity;
        this.active = active != null ? active : this.active;
    }
}
