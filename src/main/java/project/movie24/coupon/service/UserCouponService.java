package project.movie24.coupon.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.movie24.coupon.domain.Coupon;
import project.movie24.coupon.domain.CouponApplicableContext;
import project.movie24.coupon.domain.UserCoupon;
import project.movie24.coupon.domain.UserCouponStatus;
import project.movie24.coupon.repository.CouponRepository;
import project.movie24.coupon.repository.UserCouponRepository;
import project.movie24.user.domain.User;
import project.movie24.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class UserCouponService {

    private final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;
    private final UserRepository userRepository;

    public UserCoupon redeemByCode(Long userId, String code) {
        Coupon coupon = couponRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 쿠폰 코드입니다."));
        return claim(userId, coupon);
    }

    public UserCoupon claim(Long userId, Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 쿠폰입니다."));
        return claim(userId, coupon);
    }

    private UserCoupon claim(Long userId, Coupon coupon) {
        LocalDateTime now = LocalDateTime.now();
        if (!coupon.isActive()) {
            throw new IllegalStateException("발급이 종료된 쿠폰입니다.");
        }
        if (coupon.getValidFrom() != null && now.isBefore(coupon.getValidFrom())) {
            throw new IllegalStateException("아직 발급 기간이 아닙니다.");
        }
        if (coupon.getValidUntil() != null && now.isAfter(coupon.getValidUntil())) {
            throw new IllegalStateException("발급 기간이 종료된 쿠폰입니다.");
        }

        // 발급 수량 제한은 원자적 조건부 UPDATE로 선점: 0행 갱신되면 소진된 것으로 본다.
        if (couponRepository.tryIncrementIssued(coupon.getId()) == 0) {
            throw new IllegalStateException("쿠폰이 모두 소진되었습니다.");
        }

        User user = userRepository.getReferenceById(userId);
        LocalDateTime expiresAt = coupon.getValidDays() != null ? now.plusDays(coupon.getValidDays())
                : coupon.getValidUntil();

        try {
            return userCouponRepository.saveAndFlush(UserCoupon.builder()
                    .user(user)
                    .coupon(coupon)
                    .status(UserCouponStatus.UNUSED)
                    .issuedAt(now)
                    .expiresAt(expiresAt)
                    .build());
        } catch (DataIntegrityViolationException e) {
            // (user_id, coupon_id) 유니크 제약 위반 - 이미 발급받은 쿠폰. 예외가 전파되며
            // 위의 issuedQuantity 증가도 트랜잭션 롤백으로 함께 되돌아간다.
            throw new IllegalStateException("이미 발급받은 쿠폰입니다.");
        }
    }

    @Transactional(readOnly = true)
    public UserCoupon findOwned(Long userCouponId, Long userId) {
        return userCouponRepository.findByIdAndUser_Id(userCouponId, userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 쿠폰입니다."));
    }

    public List<UserCoupon> findMyCoupons(Long userId) {
        userCouponRepository.expireOutdated(userId, LocalDateTime.now());
        return userCouponRepository.findByUser_IdOrderByIssuedAtDesc(userId);
    }

    public long countUnused(Long userId) {
        userCouponRepository.expireOutdated(userId, LocalDateTime.now());
        return userCouponRepository.countByUser_IdAndStatus(userId, UserCouponStatus.UNUSED);
    }

    /**
     * 실제 차감 전 검증 + 할인 금액 계산. 결제 준비(prepare) 단계에서 호출해, 서버가 직접 계산한 금액만 신뢰한다.
     */
    @Transactional(readOnly = true)
    public int previewDiscount(UserCoupon userCoupon, int orderAmount, CouponApplicableContext context) {
        LocalDateTime now = LocalDateTime.now();
        if (userCoupon.getStatus() != UserCouponStatus.UNUSED) {
            throw new IllegalStateException("이미 사용되었거나 만료된 쿠폰입니다.");
        }
        if (userCoupon.isExpired(now)) {
            throw new IllegalStateException("만료된 쿠폰입니다.");
        }
        Coupon coupon = userCoupon.getCoupon();
        if (!coupon.isActive()) {
            throw new IllegalStateException("사용할 수 없는 쿠폰입니다.");
        }
        if (coupon.getApplicableContext() != CouponApplicableContext.BOTH && coupon.getApplicableContext() != context) {
            throw new IllegalStateException("이 결제에는 사용할 수 없는 쿠폰입니다.");
        }
        if (orderAmount < coupon.getMinPurchaseAmount()) {
            throw new IllegalStateException("최소 구매 금액(" + coupon.getMinPurchaseAmount() + "원) 미달로 사용할 수 없습니다.");
        }

        int discount = switch (coupon.getDiscountType()) {
            case PERCENT -> {
                int amount = orderAmount * coupon.getDiscountValue() / 100;
                yield coupon.getMaxDiscountAmount() != null ? Math.min(amount, coupon.getMaxDiscountAmount()) : amount;
            }
            case AMOUNT -> coupon.getDiscountValue();
        };
        return Math.max(0, Math.min(discount, orderAmount));
    }

    public UserCoupon markUsed(Long userCouponId, Long userId, int discountAmountApplied, Long reservationId) {
        UserCoupon userCoupon = findOwned(userCouponId, userId);
        if (userCoupon.getStatus() != UserCouponStatus.UNUSED) {
            throw new IllegalStateException("이미 사용된 쿠폰입니다.");
        }
        userCoupon.markUsed(LocalDateTime.now(), reservationId, discountAmountApplied);
        return userCoupon;
    }

    /**
     * 예매 취소 시 쿠폰 사용을 되돌린다. 해당 예매에 쓰인 쿠폰이 없으면 아무 일도 하지 않는다.
     */
    public void cancelUse(Long reservationId) {
        userCouponRepository.findByReservationId(reservationId)
                .ifPresent(UserCoupon::markUnused);
    }
}
