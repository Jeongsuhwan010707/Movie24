package project.movie24.showtime.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.movie24.movie.domain.Movie;
import project.movie24.movie.service.MovieService;
import project.movie24.screen.domain.Screen;
import project.movie24.screen.service.ScreenService;
import project.movie24.showtime.domain.Showtime;
import project.movie24.showtime.dto.ShowtimeRequest;
import project.movie24.showtime.repository.ShowtimeRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ShowtimeService {

    private final ShowtimeRepository showtimeRepository;
    private final MovieService movieService;
    private final ScreenService screenService;

    public Showtime register(ShowtimeRequest request) {
        Movie movie = movieService.findOne(request.getMovieId());
        Screen screen = screenService.findOne(request.getScreenId());
        Showtime showtime = Showtime.builder()
                .movie(movie)
                .screen(screen)
                .startTime(request.getStartTime())
                .basePrice(request.getBasePrice())
                .build();
        return showtimeRepository.save(showtime);
    }

    public Showtime update(Long showtimeId, ShowtimeRequest request) {
        Showtime showtime = getOrThrow(showtimeId);
        Movie movie = movieService.findOne(request.getMovieId());
        Screen screen = screenService.findOne(request.getScreenId());
        showtime.update(movie, screen, request.getStartTime(), request.getBasePrice());
        return showtime;
    }

    public void delete(Long showtimeId) {
        showtimeRepository.delete(getOrThrow(showtimeId));
    }

    @Transactional(readOnly = true)
    public Showtime findOne(Long showtimeId) {
        return getOrThrow(showtimeId);
    }

    @Transactional(readOnly = true)
    public List<Showtime> findAll() {
        return showtimeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Showtime> findByMovieId(Long movieId) {
        return showtimeRepository.findByMovieId(movieId);
    }

    @Transactional(readOnly = true)
    public List<Showtime> findByScreenId(Long screenId) {
        return showtimeRepository.findByScreenId(screenId);
    }

    @Transactional(readOnly = true)
    public List<Showtime> findByMovieIdAndDateRange(Long movieId, LocalDateTime start, LocalDateTime end) {
        return showtimeRepository.findByMovieIdAndStartTimeBetween(movieId, start, end);
    }

    private Showtime getOrThrow(Long showtimeId) {
        return showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상영시간입니다. id=" + showtimeId));
    }
}
