package project.movie24.seat.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import project.movie24.seat.domain.Seat;
import project.movie24.seat.dto.SeatGenerateRequest;
import project.movie24.seat.dto.SeatRequest;
import project.movie24.seat.dto.SeatResponse;
import project.movie24.seat.service.SeatService;

import java.util.List;

@RestController
@RequestMapping("/api/seats")
@RequiredArgsConstructor
public class SeatApiController {

    private final SeatService seatService;

    @PostMapping
    public ResponseEntity<SeatResponse> register(@Valid @RequestBody SeatRequest request) {
        Seat seat = seatService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(SeatResponse.from(seat));
    }

    @PostMapping("/generate")
    public ResponseEntity<List<SeatResponse>> generate(@Valid @RequestBody SeatGenerateRequest request) {
        List<Seat> seats = seatService.generate(request);
        List<SeatResponse> response = seats.stream().map(SeatResponse::from).toList();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<SeatResponse>> list(@RequestParam Long screenId) {
        List<Seat> seats = seatService.findByScreenId(screenId);
        return ResponseEntity.ok(seats.stream().map(SeatResponse::from).toList());
    }

    @DeleteMapping("/{seatId}")
    public ResponseEntity<Void> delete(@PathVariable Long seatId) {
        seatService.delete(seatId);
        return ResponseEntity.noContent().build();
    }
}
