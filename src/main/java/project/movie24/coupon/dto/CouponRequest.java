package project.movie24.coupon.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import project.movie24.coupon.domain.CouponApplicableContext;
import project.movie24.coupon.domain.DiscountType;

import java.time.LocalDateTime;

@Getter
public class CouponRequest {

    @NotBlank
    private String name;

    private String description;

    @NotNull
    private DiscountType discountType;

    @NotNull
    private Integer discountValue;

    private Integer maxDiscountAmount;
    private Integer minPurchaseAmount;

    @NotNull
    private CouponApplicableContext applicableContext;

    private String code;
    private Long eventId;
    private Integer validDays;
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;
    private Integer totalQuantity;
    private Boolean active;
}
