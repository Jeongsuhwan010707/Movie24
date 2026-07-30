package project.movie24.reservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import project.movie24.reservation.domain.ReservationSeat;
import project.movie24.reservation.domain.ReservationStatus;

import java.util.List;

public interface ReservationSeatRepository extends JpaRepository<ReservationSeat, Long> {

    List<ReservationSeat> findByReservationId(Long reservationId);

    List<ReservationSeat> findByShowtimeIdAndReservation_Status(Long showtimeId, ReservationStatus status);

    // 예매 목록 화면에서 좌석 라벨을 예매 건마다 따로 조회하지 않도록 여러 예매의 좌석을 한 번에 가져온다.
    @Query("select rs from ReservationSeat rs join fetch rs.seat where rs.reservation.id in :reservationIds")
    List<ReservationSeat> findByReservationIdIn(@Param("reservationIds") List<Long> reservationIds);

    // 상영시간표 화면에서 잔여 좌석 수를 상영시간마다 따로 조회하지 않도록, 여러 상영시간의 예약 좌석 수를 한 번에 센다.
    @Query("select rs.showtime.id as showtimeId, count(rs) as reservedCount from ReservationSeat rs " +
            "where rs.showtime.id in :showtimeIds and rs.reservation.status = :status " +
            "group by rs.showtime.id")
    List<ShowtimeReservedCount> countReservedSeatsByShowtimeIdIn(@Param("showtimeIds") List<Long> showtimeIds,
                                                                  @Param("status") ReservationStatus status);

    interface ShowtimeReservedCount {
        Long getShowtimeId();
        Long getReservedCount();
    }
}
