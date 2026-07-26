package project.movie24.movie.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import project.movie24.movie.domain.Movie;
import project.movie24.movie.domain.ScreeningStatus;
import project.movie24.movie.service.MovieService;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Controller
@RequestMapping("/movies")
@RequiredArgsConstructor
public class MovieController {

    private static final int PAGE_SIZE = 10;

    private final MovieService movieService;

    @GetMapping
    public String list(@RequestParam(required = false) ScreeningStatus status,
                        @RequestParam(required = false) String genre,
                        @RequestParam(defaultValue = "1") int page,
                        Model model) {
        List<Movie> allMovies = status == null ? movieService.findAll() : movieService.findByStatus(status);

        List<String> genres = allMovies.stream()
                .map(Movie::getGenre)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .toList();

        List<Movie> filtered = (genre == null || genre.isBlank())
                ? allMovies
                : allMovies.stream().filter(m -> genre.equals(m.getGenre())).toList();

        List<Movie> sorted = filtered.stream()
                .sorted(Comparator.comparing(Movie::getReleaseDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        int totalPages = Math.max(1, (int) Math.ceil(sorted.size() / (double) PAGE_SIZE));
        int currentPage = Math.min(Math.max(page, 1), totalPages);
        List<Movie> pageMovies = sorted.stream()
                .skip((long) (currentPage - 1) * PAGE_SIZE)
                .limit(PAGE_SIZE)
                .toList();

        model.addAttribute("movies", pageMovies);
        model.addAttribute("genres", genres);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedGenre", genre);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        return "movie/list";
    }
}
