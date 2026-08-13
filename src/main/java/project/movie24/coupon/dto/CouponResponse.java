package project.movie24.coupon.dto;

import lombok.Builder;
import lombok.Getter;
import project.movie24.coupon.domain.Coupon;
import project.movie24.coupon.domain.CouponApplicableContext;
import project.movie24.coupon.domain.DiscountType;

import java.time.LocalDateTime;

@Getter
@Builder
public class CouponResponse {

    private Long id;
    private String name;
    private String description;
    private DiscountType discountType;
    private Integer discountValue;
    private Integer maxDiscountAmount;
    private Integer minPurchaseAmount;
    private CouponApplicableContext applicableContext;
    private String code;
    private Long eventId;
    private Integer validDays;
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;
    private Integer totalQuantity;
    private int issuedQuantity;
    private boolean active;
    private LocalDateTime createdAt;

    public static CouponResponse from(Coupon coupon) {
        return CouponResponse.builder()
                .id(coupon.getId())
                .name(coupon.getName())
                .description(coupon.getDescription())
                .discountType(coupon.getDiscountType())
                .discountValue(coupon.getDiscountValue())
                .maxDiscountAmount(coupon.getMaxDiscountAmount())
                .minPurchaseAmount(coupon.getMinPurchaseAmount())
                .applicableContext(coupon.getApplicableContext())
                .code(coupon.getCode())
                .eventId(coupon.getEventId())
                .validDays(coupon.getValidDays())
                .validFrom(coupon.getValidFrom())
                .validUntil(coupon.getValidUntil())
                .totalQuantity(coupon.getTotalQuantity())
                .issuedQuantity(coupon.getIssuedQuantity())
                .active(coupon.isActive())
                .createdAt(coupon.getCreatedAt())
                .build();
    }
}
