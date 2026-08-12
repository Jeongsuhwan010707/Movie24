package project.movie24;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import project.movie24.movie.domain.Movie;
import project.movie24.movie.domain.ScreeningStatus;
import project.movie24.movie.repository.MovieRepository;
import project.movie24.screen.domain.Screen;
import project.movie24.screen.repository.ScreenRepository;
import project.movie24.showtime.domain.Showtime;
import project.movie24.showtime.repository.ShowtimeRepository;
import project.movie24.theater.domain.Theater;
import project.movie24.theater.repository.TheaterRepository;
import project.movie24.user.domain.EmailStatus;
import project.movie24.user.domain.Grade;
import project.movie24.user.domain.User;
import project.movie24.user.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class TestDataInit {

    // 예매 화면(movieReservation/time)에서 앞으로 이만큼의 날짜까지 상영시간표를 미리 채워둔다.
    private static final int SHOWTIME_SEED_DAYS = 15;
    private static final List<LocalTime> SHOWTIME_SLOTS = List.of(
            LocalTime.of(10, 0), LocalTime.of(13, 30), LocalTime.of(17, 50), LocalTime.of(20, 30));
    private static final Map<String, Integer> BASE_PRICE_BY_SCREEN_TYPE = Map.of(
            "2D", 12000, "3D", 14000, "IMAX", 16000, "4DX", 18000);
    private static final int DEFAULT_BASE_PRICE = 12000;

    private final UserRepository userRepository;
    private final TheaterRepository theaterRepository;
    private final MovieRepository movieRepository;
    private final ScreenRepository screenRepository;
    private final ShowtimeRepository showtimeRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        if (userRepository.findByLoginId("test").isEmpty()) {
            User user = User.builder()
                    .loginId("test")
                    .password(passwordEncoder.encode("test!"))
                    .name("테스트")
                    .nickName("닉네임")
                    .address("서울시 중구 111")
                    .email("test@naver.com")
                    .emailStatus(EmailStatus.ALLOW)
                    .build();
            userRepository.save(user);
        }

        // 영화/상영관/상영시간/좌석 관리 API가 ADMIN 등급만 호출 가능하도록 잠겨있어,
        // 관리자 화면이 생기기 전까지 로컬에서 데이터 등록/수정용으로 쓸 계정.
        if (userRepository.findByLoginId("admin").isEmpty()) {
            User admin = User.builder()
                    .loginId("admin")
                    .password(passwordEncoder.encode("admin!"))
                    .name("관리자")
                    .nickName("관리자")
                    .address("서울시 중구 111")
                    .email("admin@naver.com")
                    .emailStatus(EmailStatus.ALLOW)
                    .grade(Grade.ADMIN)
                    .build();
            userRepository.save(admin);
        }

        // 근처영화관(카카오맵) 기능 확인용으로, 서울 시내에 실제 존재하는 위치 좌표를 가진 극장을 시드해둔다.
        if (theaterRepository.count() == 0) {
            theaterRepository.saveAll(List.of(
                    Theater.builder().name("무비24 강남점").region("강남구").address("서울특별시 강남구 강남대로 396").latitude(37.4979).longitude(127.0276).build(),
                    Theater.builder().name("무비24 홍대점").region("마포구").address("서울특별시 마포구 양화로 160").latitude(37.5563).longitude(126.9220).build(),
                    Theater.builder().name("무비24 잠실점").region("송파구").address("서울특별시 송파구 올림픽로 240").latitude(37.5133).longitude(127.1000).build(),
                    Theater.builder().name("무비24 여의도점").region("영등포구").address("서울특별시 영등포구 여의대로 108").latitude(37.5219).longitude(126.9245).build(),
                    Theater.builder().name("무비24 종로점").region("종로구").address("서울특별시 종로구 종로 100").latitude(37.5704).longitude(126.9910).build(),
                    Theater.builder().name("무비24 건대점").region("광진구").address("서울특별시 광진구 능동로 110").latitude(37.5407).longitude(127.0700).build(),
                    Theater.builder().name("무비24 신촌점").region("서대문구").address("서울특별시 서대문구 신촌로 83").latitude(37.5559).longitude(126.9368).build(),
                    Theater.builder().name("무비24 왕십리점").region("성동구").address("서울특별시 성동구 왕십리로 231").latitude(37.5613).longitude(127.0378).build()
            ));
        }

        seedFutureShowtimes();
    }

    /**
     * 예매 화면 테스트용으로, 현재 상영중인 영화 x 모든 극장의 모든 상영관에 대해
     * 오늘부터 SHOWTIME_SEED_DAYS일치 상영시간표를 하루 1회씩 채운다.
     * 앞으로 예정된 상영시간이 이미 하나라도 있으면(재기동 등) 중복 생성하지 않는다.
     */
    private void seedFutureShowtimes() {
        LocalDateTime windowStart = LocalDate.now().atStartOfDay();
        if (showtimeRepository.existsByStartTimeAfter(windowStart)) {
            return;
        }

        List<Movie> movies = movieRepository.findByStatus(ScreeningStatus.NOW_SHOWING);
        List<Theater> theaters = theaterRepository.findAll();
        if (movies.isEmpty() || theaters.isEmpty()) {
            return;
        }

        List<Showtime> showtimes = new ArrayList<>();
        int movieIndex = 0;
        for (Theater theater : theaters) {
            List<Screen> screens = screenRepository.findByTheaterId(theater.getId());
            for (Screen screen : screens) {
                int basePrice = BASE_PRICE_BY_SCREEN_TYPE.getOrDefault(screen.getScreenType(), DEFAULT_BASE_PRICE);
                for (int day = 0; day < SHOWTIME_SEED_DAYS; day++) {
                    LocalDate date = LocalDate.now().plusDays(day);
                    LocalTime time = SHOWTIME_SLOTS.get(day % SHOWTIME_SLOTS.size());
                    Movie movie = movies.get(movieIndex % movies.size());
                    movieIndex++;

                    showtimes.add(Showtime.builder()
                            .movie(movie)
                            .screen(screen)
                            .startTime(LocalDateTime.of(date, time))
                            .basePrice(basePrice)
                            .build());
                }
            }
        }

        showtimeRepository.saveAll(showtimes);
    }
}
