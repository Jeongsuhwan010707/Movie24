package project.movie24.moviereservation.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import project.movie24.movie.domain.Movie;
import project.movie24.movie.domain.ScreeningStatus;
import project.movie24.movie.service.MovieService;
import project.movie24.payment.domain.PendingPayment;
import project.movie24.payment.service.PaymentService;
import project.movie24.reservation.domain.Reservation;
import project.movie24.reservation.service.ReservationService;
import project.movie24.screen.domain.Screen;
import project.movie24.screen.service.ScreenService;
import project.movie24.seat.domain.Seat;
import project.movie24.seat.service.SeatService;
import project.movie24.security.SessionAuthenticator;
import project.movie24.showtime.domain.Showtime;
import project.movie24.showtime.service.ShowtimeService;
import project.movie24.theater.domain.Theater;
import project.movie24.theater.service.TheaterService;
import project.movie24.user.domain.User;
import project.movie24.user.domain.UserPrincipal;
import project.movie24.user.repository.UserRepository;
import project.movie24.user.service.UserService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequiredArgsConstructor
public class MovieReservationController {

    private static final int DATE_RANGE_DAYS = 17;

    private final MovieService movieService;
    private final TheaterService theaterService;
    private final ScreenService screenService;
    private final ShowtimeService showtimeService;
    private final SeatService seatService;
    private final ReservationService reservationService;
    private final UserService userService;
    private final PaymentService paymentService;
    private final UserRepository userRepository;
    private final SessionAuthenticator sessionAuthenticator;

    @Value("${toss.client-key}")
    private String tossClientKey;

    @GetMapping("/movieReservation/time")
    public String time(@RequestParam(required = false) Long movieId,
                        @RequestParam(required = false) Long theaterId,
                        @RequestParam(required = false) ScheduleMode mode,
                        @RequestParam(required = false) String region,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                        @RequestParam(required = false) ScreeningStatus status,
                        HttpServletRequest request,
                        Model model) {
        // movie/theater/date/region 필터를 클릭했을 때 전체 새로고침 없이 일정 영역만 바꾸기 위해,
        // JS(fetch)로 온 요청이면 같은 템플릿의 일정 부분(fragment)만 렌더링해서 돌려준다.
        boolean fragmentOnly = "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));

        ScheduleMode selectedMode = mode != null ? mode : ScheduleMode.MOVIE;
        ScreeningStatus selectedStatus = status != null ? status : ScreeningStatus.NOW_SHOWING;
        List<Movie> movies = movieService.findByStatus(selectedStatus);

        // 실제 브라우저 새로고침/최초 진입(fragment가 아닌 전체 페이지 요청)은 항상 오늘 날짜부터
        // 보여준다. 세션 중 날짜를 이동한 상태(fragment 요청)만 요청받은 date를 그대로 존중한다.
        LocalDate selectedDate = (fragmentOnly && date != null) ? date : LocalDate.now();
        List<DateOption> dateOptions = buildDateOptions();

        List<Theater> theaters = theaterService.findAll();
        List<String> regions = theaters.stream().map(Theater::getRegion).distinct().toList();
        String selectedRegion = region != null ? region : regions.stream().findFirst().orElse(null);

        Movie selectedMovie = null;
        List<TheaterSchedule> theaterSchedules = List.of();
        List<Theater> theaterOptions = List.of();
        Theater selectedTheater = null;
        List<MovieSchedule> movieSchedules = List.of();

        if (selectedMode == ScheduleMode.THEATER) {
            // 극장별 모드는 지역으로 한 번 더 좁힐 필요 없이 전체 지점을 바로 나열한다.
            theaterOptions = theaterService.findAll();
            selectedTheater = theaterId != null ? theaterService.findOne(theaterId)
                    : theaterOptions.stream().findFirst().orElse(null);
            movieSchedules = buildMovieSchedules(selectedTheater, selectedStatus, selectedDate);
        } else {
            selectedMovie = movieId != null ? movieService.findOne(movieId)
                    : movies.stream().findFirst().orElse(null);
            theaterSchedules = buildTheaterSchedules(selectedMovie, selectedRegion, selectedDate);
        }

        model.addAttribute("mode", selectedMode);
        model.addAttribute("movies", movies);
        model.addAttribute("selectedStatus", selectedStatus);
        model.addAttribute("selectedMovie", selectedMovie);
        model.addAttribute("dateOptions", dateOptions);
        model.addAttribute("selectedDate", selectedDate);
        model.addAttribute("regions", regions);
        model.addAttribute("selectedRegion", selectedRegion);
        model.addAttribute("theaterSchedules", theaterSchedules);
        model.addAttribute("theaterOptions", theaterOptions);
        model.addAttribute("selectedTheater", selectedTheater);
        model.addAttribute("movieSchedules", movieSchedules);

        if (!fragmentOnly) {
            return "movieReservation/time";
        }
        // 날짜만 바꾼 경우, 위쪽 영화/극장 선택창·날짜 스크롤러는 그대로 두고
        // 아래 일정 목록(#scheduleList)만 바꾸면 화면이 흔들리지 않는다.
        boolean scheduleListOnly = "schedule-list".equals(request.getHeader("X-Partial"));
        return scheduleListOnly ? "movieReservation/time :: scheduleList" : "movieReservation/time :: content";
    }

    private List<DateOption> buildDateOptions() {
        return IntStream.range(0, DATE_RANGE_DAYS)
                .mapToObj(i -> {
                    LocalDate d = LocalDate.now().plusDays(i);
                    String dayName = switch (d.getDayOfWeek()) {
                        case MONDAY -> "월";
                        case TUESDAY -> "화";
                        case WEDNESDAY -> "수";
                        case THURSDAY -> "목";
                        case FRIDAY -> "금";
                        case SATURDAY -> "토";
                        case SUNDAY -> "일";
                    };
                    String weekendClass = switch (d.getDayOfWeek()) {
                        case SATURDAY -> "sat";
                        case SUNDAY -> "sun";
                        default -> "";
                    };
                    return new DateOption(d, String.valueOf(d.getDayOfMonth()), dayName, weekendClass);
                })
                .toList();
    }

    private List<TheaterSchedule> buildTheaterSchedules(Movie selectedMovie, String selectedRegion, LocalDate selectedDate) {
        if (selectedMovie == null || selectedRegion == null) {
            return List.of();
        }

        List<Showtime> showtimes = showtimeService.findByMovieAndDate(selectedMovie.getId(), startOfDay(selectedDate), endOfDay(selectedDate));
        Map<Long, Integer> reservedCounts = reservedCountsFor(showtimes);

        return theaterService.findByRegion(selectedRegion).stream()
                .map(theater -> new TheaterSchedule(theater, buildScreenSchedules(theater, showtimes, reservedCounts)))
                .filter(ts -> !ts.getScreens().isEmpty())
                .toList();
    }

    private List<ScreenSchedule> buildScreenSchedules(Theater theater, List<Showtime> showtimes, Map<Long, Integer> reservedCounts) {
        return screenService.findByTheaterId(theater.getId()).stream()
                .map(screen -> {
                    List<ShowtimeSchedule> screenShowtimes = showtimes.stream()
                            .filter(showtime -> showtime.getScreen().getId().equals(screen.getId()))
                            .sorted(Comparator.comparing(Showtime::getStartTime))
                            .map(showtime -> new ShowtimeSchedule(showtime, endTime(showtime), remainingSeats(screen, showtime, reservedCounts)))
                            .toList();
                    return new ScreenSchedule(screen, screenShowtimes);
                })
                .filter(ss -> !ss.getShowtimes().isEmpty())
                .toList();
    }

    private LocalDateTime startOfDay(LocalDate date) {
        return date.atStartOfDay();
    }

    private LocalDateTime endOfDay(LocalDate date) {
        return date.plusDays(1).atStartOfDay();
    }

    private LocalDateTime endTime(Showtime showtime) {
        Integer runtimeMinutes = showtime.getMovie().getRuntimeMinutes();
        return showtime.getStartTime().plusMinutes(runtimeMinutes != null ? runtimeMinutes : 0);
    }

    // 상영시간표 화면(하루치 전체 상영시간)에서 잔여 좌석을 한 번에 계산하기 위해, 건마다 쿼리하는 대신
    // 미리 배치로 센 예약 좌석 수(reservedCountsFor)를 조회해서 쓴다.
    private Map<Long, Integer> reservedCountsFor(List<Showtime> showtimes) {
        List<Long> showtimeIds = showtimes.stream().map(Showtime::getId).toList();
        return reservationService.countReservedSeats(showtimeIds);
    }

    private int remainingSeats(Screen screen, Showtime showtime, Map<Long, Integer> reservedCounts) {
        int reserved = reservedCounts.getOrDefault(showtime.getId(), 0);
        return Math.max(0, screen.getTotalSeats() - reserved);
    }

    private List<MovieSchedule> buildMovieSchedules(Theater selectedTheater, ScreeningStatus selectedStatus, LocalDate selectedDate) {
        if (selectedTheater == null) {
            return List.of();
        }

        List<Showtime> showtimes = showtimeService.findByTheaterAndDate(selectedTheater.getId(), startOfDay(selectedDate), endOfDay(selectedDate))
                .stream()
                .filter(showtime -> showtime.getMovie().getStatus() == selectedStatus)
                .toList();
        Map<Long, Integer> reservedCounts = reservedCountsFor(showtimes);

        List<Movie> moviesShowing = showtimes.stream()
                .map(Showtime::getMovie)
                .collect(Collectors.toMap(Movie::getId, movie -> movie, (a, b) -> a, LinkedHashMap::new))
                .values().stream()
                .sorted(Comparator.comparing(Movie::getTitle))
                .toList();

        return moviesShowing.stream()
                .map(movie -> new MovieSchedule(movie, buildScreenSchedules(movie, showtimes, reservedCounts)))
                .filter(ms -> !ms.getScreens().isEmpty())
                .toList();
    }

    private List<ScreenSchedule> buildScreenSchedules(Movie movie, List<Showtime> showtimes, Map<Long, Integer> reservedCounts) {
        List<Showtime> movieShowtimes = showtimes.stream()
                .filter(showtime -> showtime.getMovie().getId().equals(movie.getId()))
                .toList();

        List<Screen> screens = movieShowtimes.stream()
                .map(Showtime::getScreen)
                .collect(Collectors.toMap(Screen::getId, screen -> screen, (a, b) -> a, LinkedHashMap::new))
                .values().stream().toList();

        return screens.stream()
                .map(screen -> new ScreenSchedule(screen, movieShowtimes.stream()
                        .filter(showtime -> showtime.getScreen().getId().equals(screen.getId()))
                        .sorted(Comparator.comparing(Showtime::getStartTime))
                        .map(showtime -> new ShowtimeSchedule(showtime, endTime(showtime), remainingSeats(screen, showtime, reservedCounts)))
                        .toList()))
                .toList();
    }

    @GetMapping("/movieReservation/seat")
    public String seat(@RequestParam Long showtimeId, Model model) {
        Showtime showtime = showtimeService.findOne(showtimeId);
        List<Seat> seats = seatService.findByScreenId(showtime.getScreen().getId()).stream()
                .sorted(Comparator.comparing(Seat::getRowLabel).thenComparing(Seat::getSeatNumber))
                .toList();
        List<Long> reservedSeatIds = reservationService.findReservedSeatIds(showtimeId);

        Map<String, List<Seat>> seatsByRow = seats.stream()
                .collect(Collectors.groupingBy(Seat::getRowLabel, LinkedHashMap::new, Collectors.toList()));

        model.addAttribute("showtime", showtime);
        model.addAttribute("seatsByRow", seatsByRow);
        model.addAttribute("reservedSeatIds", reservedSeatIds);
        return "movieReservation/seat";
    }

    @GetMapping("/movieReservation/pay")
    public String pay(@RequestParam Long showtimeId, @RequestParam List<Long> seatIds,
                       @RequestParam(defaultValue = "0") int adultCount,
                       @RequestParam(defaultValue = "0") int teenCount,
                       @AuthenticationPrincipal UserPrincipal principal,
                       Model model) {
        Showtime showtime = showtimeService.findOne(showtimeId);
        List<Seat> seats = seatService.findAllByIds(seatIds).stream()
                .sorted(Comparator.comparing(Seat::getRowLabel).thenComparing(Seat::getSeatNumber))
                .toList();
        int totalPrice = showtime.priceFor(seats.size());
        String seatIdsCsv = seatIds.stream().map(String::valueOf).collect(Collectors.joining(","));

        model.addAttribute("showtime", showtime);
        model.addAttribute("seats", seats);
        model.addAttribute("seatIdsCsv", seatIdsCsv);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("adultCount", adultCount);
        model.addAttribute("teenCount", teenCount);
        model.addAttribute("ticketSummary", buildTicketSummary(adultCount, teenCount, seats.size()));
        model.addAttribute("tossClientKey", tossClientKey);
        // /movieReservation/**는 permitAll이라 비로그인 상태로도 접근 가능 - principal이 없으면 포인트 0/혜택 없음으로 취급한다.
        model.addAttribute("pointBalance", principal != null ? principal.getUser().getPoint() : 0);
        model.addAttribute("grade", principal != null ? principal.getUser().getGrade() : null);
        int gradeDiscountRate = principal != null ? userService.discountRateFor(principal.getUser().getGrade()) : 0;
        model.addAttribute("gradeDiscountRate", gradeDiscountRate);
        model.addAttribute("gradeDiscountEligible", principal != null && gradeDiscountRate > 0
                && !reservationService.hasUsedGradeDiscountThisMonth(principal.getUser().getId()));
        return "movieReservation/pay";
    }

    @GetMapping("/movieReservation/payRedirect")
    public String payRedirect(@RequestParam String paymentKey,
                               @RequestParam String orderId,
                               @AuthenticationPrincipal UserPrincipal principal,
                               HttpSession session,
                               HttpServletRequest request,
                               HttpServletResponse response,
                               RedirectAttributes redirectAttributes) {
        PendingPayment pending = paymentService.peekPending(session, orderId);
        try {
            if (principal == null) {
                throw new IllegalStateException("로그인이 만료되었습니다. 다시 로그인 후 시도해주세요.");
            }
            Reservation reservation = paymentService.confirmAndCreateReservation(session, principal.getUser().getId(), paymentKey, orderId);
            // 결제 과정에서 포인트/등급이 바뀌어도 세션에 저장된 principal은 결제 전 스냅샷이라
            // 바로 반영되지 않는다. 최신 User로 세션 인증 정보를 다시 심어준다.
            reAuthenticate(principal.getUser().getId(), request, response);
            return "redirect:/movieReservation/payDone?reservationId=" + reservation.getId();
        } catch (RuntimeException e) {
            redirectAttributes.addAttribute("payError", e.getMessage());
            if (pending == null) {
                return "redirect:/movieReservation/time";
            }
            redirectAttributes.addAttribute("showtimeId", pending.getShowtimeId());
            redirectAttributes.addAttribute("seatIds", pending.getSeatIds());
            return "redirect:/movieReservation/pay";
        }
    }

    private void reAuthenticate(Long userId, HttpServletRequest request, HttpServletResponse response) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        UserPrincipal newPrincipal = new UserPrincipal(user);
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(newPrincipal, null, newPrincipal.getAuthorities());
        sessionAuthenticator.authenticate(authentication, request, response);
    }

    private String buildTicketSummary(int adultCount, int teenCount, int seatCount) {
        List<String> parts = new ArrayList<>();
        if (adultCount > 0) {
            parts.add("성인 " + adultCount);
        }
        if (teenCount > 0) {
            parts.add("청소년 " + teenCount);
        }
        if (parts.isEmpty()) {
            return "좌석 " + seatCount + "매";
        }
        return String.join(" · ", parts);
    }

    @GetMapping("/movieReservation/payDone")
    public String payDone(@RequestParam(required = false) Long reservationId,
                           @AuthenticationPrincipal UserPrincipal principal,
                           Model model) {
        if (reservationId != null && principal != null) {
            try {
                Reservation reservation = reservationService.findOwned(reservationId, principal.getUser().getId());
                model.addAttribute("reservation", reservation);
                model.addAttribute("seatLabels", reservationService.findSeatLabels(reservationId));
            } catch (RuntimeException ignored) {
                // 본인 예매가 아니거나 존재하지 않으면 일반 완료 화면만 보여준다.
            }
        }
        return "movieReservation/payDone";
    }
}
