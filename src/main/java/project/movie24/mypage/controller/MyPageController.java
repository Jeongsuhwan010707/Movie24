package project.movie24.mypage.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import project.movie24.reservation.dto.ReservationResponse;
import project.movie24.reservation.service.ReservationService;
import project.movie24.store.dto.StoreOrderResponse;
import project.movie24.store.service.StoreCheckoutService;
import project.movie24.user.domain.User;
import project.movie24.user.domain.UserPrincipal;

import java.util.Comparator;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class MyPageController {

    private final ReservationService reservationService;
    private final StoreCheckoutService storeCheckoutService;

    @GetMapping("/myPage")
    public String index(@AuthenticationPrincipal UserPrincipal principal, Model model) {
        User user = principal.getUser();
        List<ReservationResponse> myReservations = reservationService.findMyReservations(user.getId()).stream()
                .sorted(Comparator.comparing(ReservationResponse::getReservedAt).reversed())
                .toList();
        List<StoreOrderResponse> myStoreOrders = storeCheckoutService.findMyOrders(user.getId());

        model.addAttribute("user", user);
        model.addAttribute("reservations", myReservations);
        model.addAttribute("storeOrders", myStoreOrders);
        return "myPage/index";
    }
}
