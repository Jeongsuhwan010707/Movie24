package project.movie24.coupon.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import project.movie24.coupon.domain.UserCoupon;
import project.movie24.coupon.domain.UserCouponStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserCouponRepository extends JpaRepository<UserCoupon, Long> {

    List<UserCoupon> findByUser_IdOrderByIssuedAtDesc(Long userId);

    long countByUser_IdAndStatus(Long userId, UserCouponStatus status);

    Optional<UserCoupon> findByIdAndUser_Id(Long userCouponId, Long userId);

    Optional<UserCoupon> findByReservationId(Long reservationId);

    boolean existsByUser_IdAndCoupon_Id(Long userId, Long couponId);

    /**
     * 별도 배치/스케줄러 없이, 마이페이지 조회 시점에 기한이 지난 미사용 쿠폰을 한 번에 EXPIRED로 정리한다.
     */
    @Modifying
    @Query("UPDATE UserCoupon uc SET uc.status = project.movie24.coupon.domain.UserCouponStatus.EXPIRED " +
            "WHERE uc.user.id = :userId AND uc.status = project.movie24.coupon.domain.UserCouponStatus.UNUSED " +
            "AND uc.expiresAt IS NOT NULL AND uc.expiresAt < :now")
    int expireOutdated(@Param("userId") Long userId, @Param("now") LocalDateTime now);
}
