package project.movie24.mypage.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import project.movie24.reservation.domain.Reservation;
import project.movie24.reservation.dto.ReservationResponse;
import project.movie24.reservation.service.ReservationService;
import project.movie24.user.domain.User;
import project.movie24.user.domain.UserPrincipal;

import java.util.Comparator;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class MyPageController {

    private final ReservationService reservationService;

    @GetMapping("/myPage")
    public String index(@AuthenticationPrincipal UserPrincipal principal, Model model) {
        User user = principal.getUser();
        List<Reservation> reservations = reservationService.findMyReservations(user.getId());
        List<ReservationResponse> myReservations = reservations.stream()
                .map(r -> ReservationResponse.from(r, reservationService.findSeatLabels(r.getId())))
                .sorted(Comparator.comparing(ReservationResponse::getReservedAt).reversed())
                .toList();

        model.addAttribute("user", user);
        model.addAttribute("reservations", myReservations);
        return "myPage/index";
    }
}
