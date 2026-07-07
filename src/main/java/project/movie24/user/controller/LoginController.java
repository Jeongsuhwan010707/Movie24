package project.movie24.user.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import project.movie24.user.domain.LoginForm;
import project.movie24.user.service.LoginService;

@Slf4j
@Controller
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;
    private final SecurityContextRepository securityContextRepository;

    @GetMapping("login")
    public String loginForm(@ModelAttribute("loginForm") LoginForm loginform){
        return "/users/loginForm";
    }

    /**
     * 로그인 이후 redirect 처리
     */
    @PostMapping("/login")
    public String loginV4(
            @Valid @ModelAttribute("loginForm") LoginForm form, BindingResult bindingResult,
            @RequestParam(defaultValue = "/", name = "redirectURL", required = false) String redirectURL,
            HttpServletRequest request, HttpServletResponse response) {
        if (bindingResult.hasErrors()) {
            return "users/loginForm";
        }
        Authentication authentication = loginService.login(form.getLoginId(), form.getPassword());
        if (authentication == null) {
            bindingResult.reject("loginFail", "아이디 또는 비밀번호가 맞지 않습니다.");
            return "users/loginForm";
        }
        //로그인 성공 처리: SecurityContext를 세션에 저장해 이후 요청에서 인증 상태를 유지한다.
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);
        //redirectURL 적용
        return "redirect:" + redirectURL;
    }
}
