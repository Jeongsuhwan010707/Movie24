package project.movie24.coupon.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import project.movie24.coupon.domain.Coupon;

import java.util.List;
import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

    Optional<Coupon> findByCode(String code);

    List<Coupon> findByEventIdAndActiveTrue(Long eventId);

    /**
     * 발급 수량 제한을 read-then-write가 아닌 원자적 조건부 UPDATE로 처리해,
     * 동시 클레임/코드등록 요청이 몰려도 totalQuantity를 초과 발급하지 않도록 한다.
     */
    @Modifying
    @Query("UPDATE Coupon c SET c.issuedQuantity = c.issuedQuantity + 1 " +
            "WHERE c.id = :id AND (c.totalQuantity IS NULL OR c.issuedQuantity < c.totalQuantity)")
    int tryIncrementIssued(@Param("id") Long id);
}
