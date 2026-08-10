package project.movie24.mypage.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import project.movie24.mypage.dto.NicknameUpdateForm;
import project.movie24.mypage.dto.ProfileUpdateForm;
import project.movie24.reservation.dto.ReservationResponse;
import project.movie24.reservation.service.ReservationService;
import project.movie24.security.SessionAuthenticator;
import project.movie24.store.dto.StoreOrderResponse;
import project.movie24.store.service.StoreCheckoutService;
import project.movie24.user.domain.EmailStatus;
import project.movie24.user.domain.User;
import project.movie24.user.domain.UserPrincipal;
import project.movie24.user.service.UserService;

import java.util.Comparator;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class MyPageController {

    private final ReservationService reservationService;
    private final StoreCheckoutService storeCheckoutService;
    private final UserService userService;
    private final SessionAuthenticator sessionAuthenticator;

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

    @GetMapping("/myPage/edit")
    public String editForm(@AuthenticationPrincipal UserPrincipal principal, Model model) {
        model.addAttribute("user", principal.getUser());
        return "myPage/edit";
    }

    @PostMapping("/myPage/edit")
    public String edit(@AuthenticationPrincipal UserPrincipal principal, ProfileUpdateForm form,
                        HttpServletRequest request, HttpServletResponse response) {
        if (isBlank(form.getPhone()) || isBlank(form.getEmail())) {
            return "redirect:/myPage/edit?error=blank";
        }
        EmailStatus emailStatus = "Y".equals(form.getEmailYn()) ? EmailStatus.ALLOW : EmailStatus.REJECT;
        User updated = userService.updateProfile(principal.getUser().getId(),
                form.getAddress(), form.getPhone(), form.getEmail(), emailStatus);
        reAuthenticate(updated, request, response);
        return "redirect:/myPage";
    }

    @GetMapping("/myPage/nickname")
    public String nicknameForm(@AuthenticationPrincipal UserPrincipal principal, Model model) {
        model.addAttribute("user", principal.getUser());
        return "myPage/nickname";
    }

    @PostMapping("/myPage/nickname")
    public String nickname(@AuthenticationPrincipal UserPrincipal principal, NicknameUpdateForm form,
                            HttpServletRequest request, HttpServletResponse response) {
        if (isBlank(form.getNickName())) {
            return "redirect:/myPage/nickname?error=blank";
        }
        User updated = userService.updateNickName(principal.getUser().getId(), form.getNickName());
        reAuthenticate(updated, request, response);
        return "redirect:/myPage";
    }

    // 세션에 저장된 principal은 수정 전 User 스냅샷이라, DB만 갱신하면 마이페이지 등에서
    // 바로 반영되지 않고 재로그인이 필요해진다. 갱신된 User로 세션 인증 정보를 다시 심어준다.
    private void reAuthenticate(User user, HttpServletRequest request, HttpServletResponse response) {
        UserPrincipal newPrincipal = new UserPrincipal(user);
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(newPrincipal, null, newPrincipal.getAuthorities());
        sessionAuthenticator.authenticate(authentication, request, response);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
