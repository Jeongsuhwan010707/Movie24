package project.movie24.coupon.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.movie24.common.EntityFinders;
import project.movie24.coupon.domain.Coupon;
import project.movie24.coupon.dto.CouponRequest;
import project.movie24.coupon.repository.CouponRepository;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;

    public Coupon register(CouponRequest request) {
        Coupon coupon = Coupon.builder()
                .name(request.getName())
                .description(request.getDescription())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .maxDiscountAmount(request.getMaxDiscountAmount())
                .minPurchaseAmount(request.getMinPurchaseAmount() != null ? request.getMinPurchaseAmount() : 0)
                .applicableContext(request.getApplicableContext())
                .code(request.getCode())
                .eventId(request.getEventId())
                .validDays(request.getValidDays())
                .validFrom(request.getValidFrom())
                .validUntil(request.getValidUntil())
                .totalQuantity(request.getTotalQuantity())
                .active(request.getActive() != null ? request.getActive() : true)
                .build();
        return couponRepository.save(coupon);
    }

    public Coupon update(Long couponId, CouponRequest request) {
        Coupon coupon = getOrThrow(couponId);
        coupon.update(request.getName(), request.getDescription(), request.getDiscountType(), request.getDiscountValue(),
                request.getMaxDiscountAmount(), request.getMinPurchaseAmount(), request.getApplicableContext(),
                request.getCode(), request.getEventId(), request.getValidDays(), request.getValidFrom(), request.getValidUntil(),
                request.getTotalQuantity(), request.getActive());
        return coupon;
    }

    public void delete(Long couponId) {
        couponRepository.delete(getOrThrow(couponId));
    }

    @Transactional(readOnly = true)
    public Coupon findOne(Long couponId) {
        return getOrThrow(couponId);
    }

    @Transactional(readOnly = true)
    public List<Coupon> findAll() {
        return couponRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Coupon> findClaimableByEvent(Long eventId) {
        return couponRepository.findByEventIdAndActiveTrue(eventId);
    }

    /**
     * 쿠폰함 코드 등록 확인 팝업에서, 실제로 발급(claim)하기 전에 쿠폰 이름/혜택을 미리 보여주기 위한 조회.
     */
    @Transactional(readOnly = true)
    public Coupon findByCode(String code) {
        return couponRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 쿠폰 코드입니다."));
    }

    private Coupon getOrThrow(Long couponId) {
        return EntityFinders.getOrThrow(couponRepository, couponId, "쿠폰");
    }
}
