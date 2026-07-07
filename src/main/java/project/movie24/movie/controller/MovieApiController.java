package project.movie24.movie.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import project.movie24.movie.domain.Movie;
import project.movie24.movie.domain.ScreeningStatus;
import project.movie24.movie.dto.MovieRequest;
import project.movie24.movie.dto.MovieResponse;
import project.movie24.movie.service.MovieService;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieApiController {

    private final MovieService movieService;

    @PostMapping
    public ResponseEntity<MovieResponse> register(@Valid @RequestBody MovieRequest request) {
        Movie movie = movieService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(MovieResponse.from(movie));
    }

    @GetMapping
    public ResponseEntity<List<MovieResponse>> list(@RequestParam(required = false) ScreeningStatus status) {
        List<Movie> movies = status == null ? movieService.findAll() : movieService.findByStatus(status);
        List<MovieResponse> response = movies.stream().map(MovieResponse::from).toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{movieId}")
    public ResponseEntity<MovieResponse> findOne(@PathVariable Long movieId) {
        return ResponseEntity.ok(MovieResponse.from(movieService.findOne(movieId)));
    }

    @PutMapping("/{movieId}")
    public ResponseEntity<MovieResponse> update(@PathVariable Long movieId, @Valid @RequestBody MovieRequest request) {
        Movie movie = movieService.update(movieId, request);
        return ResponseEntity.ok(MovieResponse.from(movie));
    }

    @DeleteMapping("/{movieId}")
    public ResponseEntity<Void> delete(@PathVariable Long movieId) {
        movieService.delete(movieId);
        return ResponseEntity.noContent().build();
    }
}
