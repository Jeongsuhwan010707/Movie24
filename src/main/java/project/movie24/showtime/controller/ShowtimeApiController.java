package project.movie24.showtime.controller;

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
import project.movie24.showtime.domain.Showtime;
import project.movie24.showtime.dto.ShowtimeRequest;
import project.movie24.showtime.dto.ShowtimeResponse;
import project.movie24.showtime.service.ShowtimeService;

import java.util.List;

@RestController
@RequestMapping("/api/showtimes")
@RequiredArgsConstructor
public class ShowtimeApiController {

    private final ShowtimeService showtimeService;

    @PostMapping
    public ResponseEntity<ShowtimeResponse> register(@Valid @RequestBody ShowtimeRequest request) {
        Showtime showtime = showtimeService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ShowtimeResponse.from(showtime));
    }

    @GetMapping
    public ResponseEntity<List<ShowtimeResponse>> list(@RequestParam(required = false) Long movieId,
                                                         @RequestParam(required = false) Long screenId) {
        List<Showtime> showtimes;
        if (movieId != null) {
            showtimes = showtimeService.findByMovieId(movieId);
        } else if (screenId != null) {
            showtimes = showtimeService.findByScreenId(screenId);
        } else {
            showtimes = showtimeService.findAll();
        }
        return ResponseEntity.ok(showtimes.stream().map(ShowtimeResponse::from).toList());
    }

    @GetMapping("/{showtimeId}")
    public ResponseEntity<ShowtimeResponse> findOne(@PathVariable Long showtimeId) {
        return ResponseEntity.ok(ShowtimeResponse.from(showtimeService.findOne(showtimeId)));
    }

    @PutMapping("/{showtimeId}")
    public ResponseEntity<ShowtimeResponse> update(@PathVariable Long showtimeId, @Valid @RequestBody ShowtimeRequest request) {
        Showtime showtime = showtimeService.update(showtimeId, request);
        return ResponseEntity.ok(ShowtimeResponse.from(showtime));
    }

    @DeleteMapping("/{showtimeId}")
    public ResponseEntity<Void> delete(@PathVariable Long showtimeId) {
        showtimeService.delete(showtimeId);
        return ResponseEntity.noContent().build();
    }
}
