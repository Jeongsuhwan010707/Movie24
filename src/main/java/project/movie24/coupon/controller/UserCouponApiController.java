package project.movie24.coupon.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import project.movie24.coupon.domain.UserCoupon;
import project.movie24.coupon.dto.CodeRedeemRequest;
import project.movie24.coupon.dto.CouponResponse;
import project.movie24.coupon.dto.UserCouponResponse;
import project.movie24.coupon.service.CouponService;
import project.movie24.coupon.service.UserCouponService;
import project.movie24.user.domain.UserPrincipal;

@RestController
@RequestMapping("/api/my-coupons")
@RequiredArgsConstructor
public class UserCouponApiController {

    private final UserCouponService userCouponService;
    private final CouponService couponService;

    // 코드 등록 확인 팝업에서, 실제로 발급받기 전에 쿠폰 이름/혜택을 미리 보여주기 위한 조회.
    @GetMapping("/preview")
    public ResponseEntity<CouponResponse> preview(@RequestParam String code) {
        return ResponseEntity.ok(CouponResponse.from(couponService.findByCode(code)));
    }

    @PostMapping("/redeem")
    public ResponseEntity<UserCouponResponse> redeem(@AuthenticationPrincipal UserPrincipal principal,
                                                       @Valid @RequestBody CodeRedeemRequest request) {
        UserCoupon userCoupon = userCouponService.redeemByCode(principal.getUser().getId(), request.getCode());
        return ResponseEntity.status(HttpStatus.CREATED).body(UserCouponResponse.from(userCoupon));
    }

    @PostMapping("/claim/{couponId}")
    public ResponseEntity<UserCouponResponse> claim(@AuthenticationPrincipal UserPrincipal principal,
                                                      @PathVariable Long couponId) {
        UserCoupon userCoupon = userCouponService.claim(principal.getUser().getId(), couponId);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserCouponResponse.from(userCoupon));
    }
}
