package project.movie24.user.controller;

import jakarta.servlet.http.HttpServletRequest;
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

    private final UserService userService;

    @GetMapping("/find-account")
    public String findAccountForm() {
        return "users/findAccount";
    }

    @GetMapping("/reset-password")
    public String resetPasswordForm(Model model, HttpServletRequest request) {
        if (PasswordResetSession.verifiedUserId(request) == null) {
            return "redirect:/users/find-account";
        }
        model.addAttribute("resetPasswordForm", new ResetPasswordForm());
        return "users/resetPassword";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@Valid @ModelAttribute("resetPasswordForm") ResetPasswordForm form, BindingResult bindingResult,
                                 Model model, HttpServletRequest request) {
        Long userId = PasswordResetSession.verifiedUserId(request);
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
        PasswordResetSession.clear(request);
        return "redirect:/login?resetSuccess";
    }
}
