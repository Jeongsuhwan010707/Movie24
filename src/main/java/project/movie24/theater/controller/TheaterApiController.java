package project.movie24.theater.controller;

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
import project.movie24.theater.domain.Theater;
import project.movie24.theater.dto.TheaterRequest;
import project.movie24.theater.dto.TheaterResponse;
import project.movie24.theater.service.TheaterService;

import java.util.List;

@RestController
@RequestMapping("/api/theaters")
@RequiredArgsConstructor
public class TheaterApiController {

    private final TheaterService theaterService;

    @PostMapping
    public ResponseEntity<TheaterResponse> register(@Valid @RequestBody TheaterRequest request) {
        Theater theater = theaterService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(TheaterResponse.from(theater));
    }

    @GetMapping
    public ResponseEntity<List<TheaterResponse>> list(@RequestParam(required = false) String region) {
        List<Theater> theaters = region == null ? theaterService.findAll() : theaterService.findByRegion(region);
        return ResponseEntity.ok(theaters.stream().map(TheaterResponse::from).toList());
    }

    @GetMapping("/{theaterId}")
    public ResponseEntity<TheaterResponse> findOne(@PathVariable Long theaterId) {
        return ResponseEntity.ok(TheaterResponse.from(theaterService.findOne(theaterId)));
    }

    @PutMapping("/{theaterId}")
    public ResponseEntity<TheaterResponse> update(@PathVariable Long theaterId, @Valid @RequestBody TheaterRequest request) {
        Theater theater = theaterService.update(theaterId, request);
        return ResponseEntity.ok(TheaterResponse.from(theater));
    }

    @DeleteMapping("/{theaterId}")
    public ResponseEntity<Void> delete(@PathVariable Long theaterId) {
        theaterService.delete(theaterId);
        return ResponseEntity.noContent().build();
    }
}
