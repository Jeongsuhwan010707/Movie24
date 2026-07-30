package project.movie24.user.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import project.movie24.user.domain.ResetPasswordForm;
import project.movie24.user.service.UserService;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class FindAccountController {

    // 비밀번호 재설정 본인확인(FindAccountApiController#verifyForPasswordReset)이 세션에 심어두는 키.
    static final String RESET_USER_ID_SESSION_KEY = "passwordResetUserId";

    private final UserService userService;

    @GetMapping("/find-account")
    public String findAccountForm() {
        return "users/findAccount";
    }

    @GetMapping("/reset-password")
    public String resetPasswordForm(Model model, HttpServletRequest request) {
        if (request.getSession(false) == null
                || request.getSession(false).getAttribute(RESET_USER_ID_SESSION_KEY) == null) {
            return "redirect:/users/find-account";
        }
        model.addAttribute("resetPasswordForm", new ResetPasswordForm());
        return "users/resetPassword";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@Valid @ModelAttribute("resetPasswordForm") ResetPasswordForm form, BindingResult bindingResult,
                                 Model model, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Long userId = session == null ? null : (Long) session.getAttribute(RESET_USER_ID_SESSION_KEY);
        if (userId == null) {
            return "redirect:/users/find-account";
        }

        if (bindingResult.hasErrors()) {
            return "users/resetPassword";
        }
        if (!form.getNewPassword().equals(form.getNewPasswordConfirm())) {
            model.addAttribute("resetPasswordError", "비밀번호가 일치하지 않습니다.");
            return "users/resetPassword";
        }

        userService.resetPassword(userId, form.getNewPassword());
        session.removeAttribute(RESET_USER_ID_SESSION_KEY);
        return "redirect:/login?resetSuccess";
    }
}
