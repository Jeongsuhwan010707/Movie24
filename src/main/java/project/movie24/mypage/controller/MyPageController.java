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
import org.springframework.web.bind.annotation.RequestParam;
import project.movie24.coupon.domain.UserCoupon;
import project.movie24.coupon.domain.UserCouponStatus;
import project.movie24.coupon.service.UserCouponService;
import project.movie24.mypage.dto.NicknameUpdateForm;
import project.movie24.mypage.dto.ProfileUpdateForm;
import project.movie24.point.service.PointService;
import project.movie24.reservation.dto.ReservationResponse;
import project.movie24.reservation.service.ReservationService;
import project.movie24.security.SessionAuthenticator;
import project.movie24.store.dto.StoreOrderResponse;
import project.movie24.store.service.StoreCheckoutService;
import project.movie24.user.domain.EmailStatus;
import project.movie24.user.domain.User;
import project.movie24.user.domain.UserPrincipal;
import project.movie24.user.service.UserService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class MyPageController {

    private final ReservationService reservationService;
    private final StoreCheckoutService storeCheckoutService;
    private final UserService userService;
    private final PointService pointService;
    private final UserCouponService userCouponService;
    private final SessionAuthenticator sessionAuthenticator;

    @GetMapping("/myPage")
    public String index(@AuthenticationPrincipal UserPrincipal principal, Model model) {
        User user = principal.getUser();
        List<ReservationResponse> myReservations = reservationService.findMyReservations(user.getId()).stream()
                .sorted(Comparator.comparing(ReservationResponse::getReservedAt).reversed())
                .toList();
        List<StoreOrderResponse> myStoreOrders = storeCheckoutService.findMyOrders(user.getId());

        long recentSpend = reservationService.sumPaidAmountLastYear(user.getId());
        Integer nextGradeThreshold = userService.nextGradeThreshold(user.getGrade());

        model.addAttribute("user", user);
        model.addAttribute("reservations", myReservations);
        model.addAttribute("storeOrders", myStoreOrders);
        model.addAttribute("recentSpend", recentSpend);
        model.addAttribute("nextGradeThreshold", nextGradeThreshold);
        model.addAttribute("unusedCouponCount", userCouponService.countUnused(user.getId()));
        return "myPage/index";
    }

    @GetMapping("/myPage/points")
    public String points(@AuthenticationPrincipal UserPrincipal principal, Model model) {
        model.addAttribute("user", principal.getUser());
        model.addAttribute("pointHistory", pointService.findHistory(principal.getUser().getId()));
        return "myPage/points";
    }

    @GetMapping("/myPage/coupons")
    public String coupons(@AuthenticationPrincipal UserPrincipal principal,
                           @RequestParam(required = false) String error,
                           Model model) {
        List<UserCoupon> myCoupons = userCouponService.findMyCoupons(principal.getUser().getId()).stream()
                // 발급일 최신순은 유지하면서, 사용 가능한(UNUSED) 쿠폰이 위로 오도록 상태로 한 번 더 정렬한다.
                .sorted(Comparator.comparing(uc -> uc.getStatus().ordinal()))
                .toList();
        long unusedCount = myCoupons.stream().filter(uc -> uc.getStatus() == UserCouponStatus.UNUSED).count();

        model.addAttribute("user", principal.getUser());
        model.addAttribute("myCoupons", myCoupons);
        model.addAttribute("unusedCount", unusedCount);
        model.addAttribute("error", error);
        return "myPage/coupons";
    }

    @PostMapping("/myPage/coupons/redeem")
    public String redeemCoupon(@AuthenticationPrincipal UserPrincipal principal, @RequestParam String code) {
        if (isBlank(code)) {
            return "redirect:/myPage/coupons?error=" + encode("쿠폰 코드를 입력해주세요.");
        }
        try {
            userCouponService.redeemByCode(principal.getUser().getId(), code.trim());
        } catch (RuntimeException e) {
            return "redirect:/myPage/coupons?error=" + encode(e.getMessage());
        }
        return "redirect:/myPage/coupons";
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

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
