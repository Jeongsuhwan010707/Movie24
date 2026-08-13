package project.movie24.coupon.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import project.movie24.coupon.domain.Coupon;
import project.movie24.coupon.dto.CouponRequest;
import project.movie24.coupon.dto.CouponResponse;
import project.movie24.coupon.service.CouponService;

import java.util.List;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponApiController {

    private final CouponService couponService;

    @PostMapping
    public ResponseEntity<CouponResponse> register(@Valid @RequestBody CouponRequest request) {
        Coupon coupon = couponService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(CouponResponse.from(coupon));
    }

    @GetMapping
    public ResponseEntity<List<CouponResponse>> list() {
        return ResponseEntity.ok(couponService.findAll().stream().map(CouponResponse::from).toList());
    }

    @GetMapping("/{couponId}")
    public ResponseEntity<CouponResponse> findOne(@PathVariable Long couponId) {
        return ResponseEntity.ok(CouponResponse.from(couponService.findOne(couponId)));
    }

    @PutMapping("/{couponId}")
    public ResponseEntity<CouponResponse> update(@PathVariable Long couponId, @Valid @RequestBody CouponRequest request) {
        Coupon coupon = couponService.update(couponId, request);
        return ResponseEntity.ok(CouponResponse.from(coupon));
    }

    @DeleteMapping("/{couponId}")
    public ResponseEntity<Void> delete(@PathVariable Long couponId) {
        couponService.delete(couponId);
        return ResponseEntity.noContent().build();
    }

    // 이벤트 상세 페이지의 "쿠폰받기" 버튼이 호출하는 유일한 공개(permitAll) 엔드포인트.
    @GetMapping("/claimable")
    public ResponseEntity<List<CouponResponse>> claimable(@RequestParam Long eventId) {
        return ResponseEntity.ok(couponService.findClaimableByEvent(eventId).stream().map(CouponResponse::from).toList());
    }
}
