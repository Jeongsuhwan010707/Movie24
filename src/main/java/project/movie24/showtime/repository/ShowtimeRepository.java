package project.movie24.showtime.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import project.movie24.showtime.domain.Showtime;

import java.time.LocalDateTime;
import java.util.List;

public interface ShowtimeRepository extends JpaRepository<Showtime, Long> {

    List<Showtime> findByMovieId(Long movieId);

    List<Showtime> findByScreenId(Long screenId);

    boolean existsByStartTimeAfter(LocalDateTime time);

    // 상영시간표 화면(movieReservation/time)이 이 목록을 그대로 순회하며 movie/screen/theater 필드를
    // 읽으므로, 지연로딩으로 건마다 추가 쿼리가 나가지 않도록 한 번에 조인해서 가져온다.
    @Query("select s from Showtime s " +
            "join fetch s.movie " +
            "join fetch s.screen sc " +
            "join fetch sc.theater " +
            "where s.movie.id = :movieId and s.startTime between :start and :end")
    List<Showtime> findByMovieIdAndStartTimeBetween(@Param("movieId") Long movieId,
                                                     @Param("start") LocalDateTime start,
                                                     @Param("end") LocalDateTime end);

    @Query("select s from Showtime s " +
            "join fetch s.movie " +
            "join fetch s.screen sc " +
            "join fetch sc.theater " +
            "where sc.theater.id = :theaterId and s.startTime between :start and :end")
    List<Showtime> findByScreen_Theater_IdAndStartTimeBetween(@Param("theaterId") Long theaterId,
                                                               @Param("start") LocalDateTime start,
                                                               @Param("end") LocalDateTime end);
}
