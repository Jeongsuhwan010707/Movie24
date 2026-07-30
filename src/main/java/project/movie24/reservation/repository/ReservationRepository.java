package project.movie24.reservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import project.movie24.reservation.domain.Reservation;

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
}
