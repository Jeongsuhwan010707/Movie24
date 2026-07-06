package project.movie24.reservation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.movie24.reservation.domain.Reservation;
import project.movie24.reservation.dto.ReservationRequest;
import project.movie24.reservation.dto.ReservationResponse;
import project.movie24.reservation.dto.SeatAvailabilityResponse;
import project.movie24.reservation.service.ReservationService;
import project.movie24.user.domain.UserPrincipal;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationApiController {

    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<ReservationResponse> reserve(@AuthenticationPrincipal UserPrincipal principal,
                                                         @Valid @RequestBody ReservationRequest request) {
        Reservation reservation = reservationService.reserve(principal.getUser().getId(), request);
        List<String> seatLabels = reservationService.findSeatLabels(reservation.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ReservationResponse.from(reservation, seatLabels));
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> myReservations(@AuthenticationPrincipal UserPrincipal principal) {
        List<Reservation> reservations = reservationService.findMyReservations(principal.getUser().getId());
        List<ReservationResponse> response = reservations.stream()
                .map(r -> ReservationResponse.from(r, reservationService.findSeatLabels(r.getId())))
                .toList();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{reservationId}")
    public ResponseEntity<Void> cancel(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long reservationId) {
        reservationService.cancel(reservationId, principal.getUser().getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/showtimes/{showtimeId}/seats")
    public ResponseEntity<SeatAvailabilityResponse> seatAvailability(@PathVariable Long showtimeId) {
        List<Long> reservedSeatIds = reservationService.findReservedSeatIds(showtimeId);
        return ResponseEntity.ok(SeatAvailabilityResponse.builder()
                .showtimeId(showtimeId)
                .reservedSeatIds(reservedSeatIds)
                .build());
    }
}
