package project.movie24.coupon.dto;

import lombok.Builder;
import lombok.Getter;
import project.movie24.coupon.domain.CouponApplicableContext;
import project.movie24.coupon.domain.DiscountType;
import project.movie24.coupon.domain.UserCoupon;
import project.movie24.coupon.domain.UserCouponStatus;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserCouponResponse {

    private Long userCouponId;
    private Long couponId;
    private String name;
    private String description;
    private DiscountType discountType;
    private Integer discountValue;
    private Integer maxDiscountAmount;
    private Integer minPurchaseAmount;
    private CouponApplicableContext applicableContext;
    private UserCouponStatus status;
    private LocalDateTime issuedAt;
    private LocalDateTime expiresAt;
    private LocalDateTime usedAt;

    public static UserCouponResponse from(UserCoupon userCoupon) {
        return UserCouponResponse.builder()
                .userCouponId(userCoupon.getId())
                .couponId(userCoupon.getCoupon().getId())
                .name(userCoupon.getCoupon().getName())
                .description(userCoupon.getCoupon().getDescription())
                .discountType(userCoupon.getCoupon().getDiscountType())
                .discountValue(userCoupon.getCoupon().getDiscountValue())
                .maxDiscountAmount(userCoupon.getCoupon().getMaxDiscountAmount())
                .minPurchaseAmount(userCoupon.getCoupon().getMinPurchaseAmount())
                .applicableContext(userCoupon.getCoupon().getApplicableContext())
                .status(userCoupon.getStatus())
                .issuedAt(userCoupon.getIssuedAt())
                .expiresAt(userCoupon.getExpiresAt())
                .usedAt(userCoupon.getUsedAt())
                .build();
    }
}
