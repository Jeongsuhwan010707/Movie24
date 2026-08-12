package project.movie24.reservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import project.movie24.reservation.domain.Reservation;
import project.movie24.reservation.domain.ReservationStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // 마이페이지/예매내역 목록에서 상영시간·영화·상영관·극장을 지연로딩으로 한 건씩 더 조회하지 않도록 한 번에 조인해온다.
    @Query("select r from Reservation r " +
            "join fetch r.showtime s " +
            "join fetch s.movie " +
            "join fetch s.screen sc " +
            "join fetch sc.theater " +
            "where r.user.id = :userId")
    List<Reservation> findByUserIdWithDetails(@Param("userId") Long userId);

    // VIP 등급 재계산용으로 최근 1년간 취소되지 않은 예매 결제금액을 합산한다.
    @Query("select coalesce(sum(r.totalPrice), 0) from Reservation r " +
            "where r.user.id = :userId and r.status = :status and r.reservedAt >= :since")
    long sumPaidAmountSince(@Param("userId") Long userId, @Param("status") ReservationStatus status,
                             @Param("since") LocalDateTime since);

    // 등급 할인 월 1회 제한 판정용: 취소되지 않은 예매 중 이번 달에 등급 할인을 적용한 건이 있는지 확인한다.
    // 예매가 취소되면 자연히 대상에서 빠져 그 달에 다시 할인을 쓸 수 있다.
    boolean existsByUser_IdAndGradeDiscountAmountGreaterThanAndStatusAndReservedAtAfter(
            Long userId, int threshold, ReservationStatus status, LocalDateTime monthStart);
}
