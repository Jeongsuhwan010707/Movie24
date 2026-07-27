package project.movie24.moviereservation.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import project.movie24.movie.domain.Movie;
import project.movie24.movie.domain.ScreeningStatus;
import project.movie24.movie.service.MovieService;
import project.movie24.reservation.domain.Reservation;
import project.movie24.reservation.service.ReservationService;
import project.movie24.screen.domain.Screen;
import project.movie24.screen.service.ScreenService;
import project.movie24.seat.domain.Seat;
import project.movie24.seat.service.SeatService;
import project.movie24.showtime.domain.Showtime;
import project.movie24.showtime.service.ShowtimeService;
import project.movie24.theater.domain.Theater;
import project.movie24.theater.service.TheaterService;
import project.movie24.user.domain.UserPrincipal;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequiredArgsConstructor
public class MovieReservationController {

    private static final int DATE_RANGE_DAYS = 14;

    private final MovieService movieService;
    private final TheaterService theaterService;
    private final ScreenService screenService;
    private final ShowtimeService showtimeService;
    private final SeatService seatService;
    private final ReservationService reservationService;

    @GetMapping("/movieReservation/time")
    public String time(@RequestParam(required = false) Long movieId,
                        @RequestParam(required = false) Long theaterId,
                        @RequestParam(required = false) ScheduleMode mode,
                        @RequestParam(required = false) String region,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                        @RequestParam(required = false) ScreeningStatus status,
                        HttpServletRequest request,
                        Model model) {
        ScheduleMode selectedMode = mode != null ? mode : ScheduleMode.MOVIE;
        ScreeningStatus selectedStatus = status != null ? status : ScreeningStatus.NOW_SHOWING;
        List<Movie> movies = movieService.findByStatus(selectedStatus);

        LocalDate selectedDate = date != null ? date : LocalDate.now();
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
            theaterOptions = selectedRegion != null ? theaterService.findByRegion(selectedRegion) : List.of();
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

        // movie/theater/date/region 필터를 클릭했을 때 전체 새로고침 없이 일정 영역만 바꾸기 위해,
        // JS(fetch)로 온 요청이면 같은 템플릿의 일정 부분(fragment)만 렌더링해서 돌려준다.
        boolean fragmentOnly = "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
        return fragmentOnly ? "movieReservation/time :: content" : "movieReservation/time";
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

        LocalDateTime startOfDay = selectedDate.atStartOfDay();
        LocalDateTime endOfDay = selectedDate.plusDays(1).atStartOfDay();
        List<Showtime> showtimes = showtimeService.findByMovieIdAndDateRange(selectedMovie.getId(), startOfDay, endOfDay);

        return theaterService.findByRegion(selectedRegion).stream()
                .map(theater -> new TheaterSchedule(theater, buildScreenSchedules(theater, showtimes)))
                .filter(ts -> !ts.getScreens().isEmpty())
                .toList();
    }

    private List<ScreenSchedule> buildScreenSchedules(Theater theater, List<Showtime> showtimes) {
        return screenService.findByTheaterId(theater.getId()).stream()
                .map(screen -> {
                    List<Showtime> screenShowtimes = showtimes.stream()
                            .filter(showtime -> showtime.getScreen().getId().equals(screen.getId()))
                            .sorted(Comparator.comparing(Showtime::getStartTime))
                            .toList();
                    return new ScreenSchedule(screen, screenShowtimes);
                })
                .filter(ss -> !ss.getShowtimes().isEmpty())
                .toList();
    }

    private List<MovieSchedule> buildMovieSchedules(Theater selectedTheater, ScreeningStatus selectedStatus, LocalDate selectedDate) {
        if (selectedTheater == null) {
            return List.of();
        }

        LocalDateTime startOfDay = selectedDate.atStartOfDay();
        LocalDateTime endOfDay = selectedDate.plusDays(1).atStartOfDay();
        List<Showtime> showtimes = showtimeService.findByTheaterIdAndDateRange(selectedTheater.getId(), startOfDay, endOfDay)
                .stream()
                .filter(showtime -> showtime.getMovie().getStatus() == selectedStatus)
                .toList();

        List<Movie> moviesShowing = showtimes.stream()
                .map(Showtime::getMovie)
                .collect(Collectors.toMap(Movie::getId, movie -> movie, (a, b) -> a, LinkedHashMap::new))
                .values().stream()
                .sorted(Comparator.comparing(Movie::getTitle))
                .toList();

        return moviesShowing.stream()
                .map(movie -> new MovieSchedule(movie, buildScreenSchedulesForMovie(movie, showtimes)))
                .filter(ms -> !ms.getScreens().isEmpty())
                .toList();
    }

    private List<ScreenSchedule> buildScreenSchedulesForMovie(Movie movie, List<Showtime> showtimes) {
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
                       Model model) {
        Showtime showtime = showtimeService.findOne(showtimeId);
        List<Seat> seats = seatService.findAllByIds(seatIds).stream()
                .sorted(Comparator.comparing(Seat::getRowLabel).thenComparing(Seat::getSeatNumber))
                .toList();
        int totalPrice = showtime.getBasePrice() * seats.size();
        String seatIdsCsv = seatIds.stream().map(String::valueOf).collect(Collectors.joining(","));

        model.addAttribute("showtime", showtime);
        model.addAttribute("seats", seats);
        model.addAttribute("seatIdsCsv", seatIdsCsv);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("adultCount", adultCount);
        model.addAttribute("teenCount", teenCount);
        model.addAttribute("ticketSummary", buildTicketSummary(adultCount, teenCount, seats.size()));
        return "movieReservation/pay";
    }

    private String buildTicketSummary(int adultCount, int teenCount, int seatCount) {
        List<String> parts = new java.util.ArrayList<>();
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
