package project.movie24.reservation.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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
import project.movie24.security.SessionAuthenticator;
import project.movie24.user.domain.User;
import project.movie24.user.domain.UserPrincipal;
import project.movie24.user.repository.UserRepository;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationApiController {

    private final ReservationService reservationService;
    private final UserRepository userRepository;
    private final SessionAuthenticator sessionAuthenticator;

    @PostMapping
    public ResponseEntity<ReservationResponse> reserve(@AuthenticationPrincipal UserPrincipal principal,
                                                         @Valid @RequestBody ReservationRequest request) {
        Reservation reservation = reservationService.reserve(principal.getUser().getId(), request.getShowtimeId(), request.getSeatIds());
        List<String> seatLabels = reservationService.findSeatLabels(reservation.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ReservationResponse.from(reservation, seatLabels));
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> myReservations(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(reservationService.findMyReservations(principal.getUser().getId()));
    }

    @DeleteMapping("/{reservationId}")
    public ResponseEntity<Void> cancel(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long reservationId,
                                        HttpServletRequest request, HttpServletResponse response) {
        reservationService.cancel(reservationId, principal.getUser().getId());
        // 취소로 포인트가 환급/회수될 수 있어, 세션에 남아있는 결제 전 principal 스냅샷을 최신 User로 갱신한다.
        reAuthenticate(principal.getUser().getId(), request, response);
        return ResponseEntity.noContent().build();
    }

    private void reAuthenticate(Long userId, HttpServletRequest request, HttpServletResponse response) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        UserPrincipal newPrincipal = new UserPrincipal(user);
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(newPrincipal, null, newPrincipal.getAuthorities());
        sessionAuthenticator.authenticate(authentication, request, response);
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
