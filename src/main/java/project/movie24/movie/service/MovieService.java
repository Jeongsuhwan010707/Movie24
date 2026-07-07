package project.movie24.movie.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.movie24.movie.domain.Movie;
import project.movie24.movie.domain.ScreeningStatus;
import project.movie24.movie.dto.MovieRequest;
import project.movie24.movie.repository.MovieRepository;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;

    public Movie register(MovieRequest request) {
        Movie movie = Movie.builder()
                .title(request.getTitle())
                .director(request.getDirector())
                .actors(request.getActors())
                .genre(request.getGenre())
                .ageRating(request.getAgeRating())
                .runtimeMinutes(request.getRuntimeMinutes())
                .country(request.getCountry())
                .releaseDate(request.getReleaseDate())
                .synopsis(request.getSynopsis())
                .posterImageUrl(request.getPosterImageUrl())
                .trailerVideoUrl(request.getTrailerVideoUrl())
                .status(request.getStatus())
                .build();
        return movieRepository.save(movie);
    }

    public Movie update(Long movieId, MovieRequest request) {
        Movie movie = getOrThrow(movieId);
        movie.update(request.getTitle(), request.getDirector(), request.getActors(), request.getGenre(),
                request.getAgeRating(), request.getRuntimeMinutes(), request.getCountry(),
                request.getReleaseDate(), request.getSynopsis(), request.getPosterImageUrl(),
                request.getTrailerVideoUrl(), request.getStatus());
        return movie;
    }

    public void delete(Long movieId) {
        movieRepository.delete(getOrThrow(movieId));
    }

    @Transactional(readOnly = true)
    public Movie findOne(Long movieId) {
        return getOrThrow(movieId);
    }

    @Transactional(readOnly = true)
    public List<Movie> findAll() {
        return movieRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Movie> findByStatus(ScreeningStatus status) {
        return movieRepository.findByStatus(status);
    }

    private Movie getOrThrow(Long movieId) {
        return movieRepository.findById(movieId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 영화입니다. id=" + movieId));
    }
}
