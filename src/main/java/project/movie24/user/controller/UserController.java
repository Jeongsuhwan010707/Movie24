package project.movie24.user.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import project.movie24.user.domain.CustomOAuth2User;
import project.movie24.user.domain.EmailStatus;
import project.movie24.user.domain.User;
import project.movie24.user.domain.UserForm;
import project.movie24.user.service.LoginService;
import project.movie24.user.service.UserService;

@RequestMapping("/users")
@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final LoginService loginService;
    private final SecurityContextRepository securityContextRepository;

    @GetMapping("/terms")
    public String joinForm(){
        return "/users/terms";
    }

    @GetMapping("/authentication")
    public String authenticationForm(){
        return "/users/authentication";
    }

    @GetMapping("/new")
    public String createForm(Model model){
        User socialUser = getPendingSocialUser();
        boolean socialSignup = socialUser != null;

        UserForm form = new UserForm();
        if (socialSignup) {
            // 실제 loginId는 "구글_providerId" 형식이라 사람이 읽기엔 의미가 없으므로,
            // 화면에는(readonly) 이메일을 대신 보여준다. 저장 시엔 아래 POST에서 다시
            // pendingSocialUser의 진짜 loginId를 쓰므로 이 값은 표시 전용이다.
            form.setLoginId(socialUser.getEmail());
            form.setEmail(socialUser.getEmail());
            form.setName(socialUser.getName());
            form.setNickName(socialUser.getNickName());
            form.setPhone(socialUser.getPhone());
            form.setGender(socialUser.getGender());
            form.setBirthDate(socialUser.getBirthDate());
        }

        model.addAttribute("user", form);
        model.addAttribute("socialSignup", socialSignup);
        // 카카오/구글은 이메일·닉네임 정도만 주고 네이버는 더 많이 주기 때문에,
        // 공급자가 실제로 값을 준 항목만 개별적으로 readonly 처리한다.
        model.addAttribute("nameLocked", socialSignup && hasText(socialUser.getName()));
        model.addAttribute("phoneLocked", socialSignup && hasText(socialUser.getPhone()));
        model.addAttribute("genderLocked", socialSignup && hasText(socialUser.getGender()));
        model.addAttribute("birthDateLocked", socialSignup && socialUser.getBirthDate() != null);
        return "/users/userForm";
    }

    @PostMapping("/new")
    public String create(UserForm userForm, HttpServletRequest request, HttpServletResponse response){
        if ("Y".equals(userForm.getEmailYn())) {
            userForm.setEmailStatus(EmailStatus.ALLOW);
        } else {
            userForm.setEmailStatus(EmailStatus.REJECT);
        }

        User pendingSocialUser = getPendingSocialUser();

        User.UserBuilder userBuilder = User.builder()
                .name(userForm.getName())
                .nickName(userForm.getNickName())
                .address(userForm.getAddress())
                .phone(userForm.getPhone())
                .emailStatus(userForm.getEmailStatus())
                .gender(userForm.getGender())
                .birthDate(userForm.getBirthDate());

        if (pendingSocialUser != null) {
            // 아이디/이메일/연동 정보는 readonly로 보여준 화면값이라도 그대로 믿지 않고,
            // 소셜 로그인 시점에 공급자로부터 받아둔 값을 사용해 저장한다(위변조 방지).
            userBuilder.loginId(pendingSocialUser.getLoginId())
                    .email(pendingSocialUser.getEmail())
                    .provider(pendingSocialUser.getProvider())
                    .providerId(pendingSocialUser.getProviderId());
        } else {
            userBuilder.loginId(userForm.getLoginId())
                    .password(userForm.getPassword())
                    .email(userForm.getEmail());
        }

        User saved = userService.saveUser(userBuilder.build());

        Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
        if (pendingSocialUser != null && currentAuth instanceof OAuth2AuthenticationToken oldToken) {
            // 임시(미저장) 소셜 principal을, 방금 저장되어 id가 생긴 principal로 교체한다.
            CustomOAuth2User newPrincipal = new CustomOAuth2User(saved,
                    ((CustomOAuth2User) oldToken.getPrincipal()).getAttributes());
            setAuthenticatedSession(new OAuth2AuthenticationToken(
                    newPrincipal, newPrincipal.getAuthorities(), oldToken.getAuthorizedClientRegistrationId()
            ), request, response);
        } else {
            // 가입 직후 자동 로그인: 인코딩 전 원본 비밀번호로 인증해 세션에 저장한다.
            Authentication authentication = loginService.login(userForm.getLoginId(), userForm.getPassword());
            if (authentication != null) {
                setAuthenticatedSession(authentication, request, response);
            }
        }

        return "redirect:/users/complete";
    }

    @GetMapping("/complete")
    public String completeForm(){
        return "/users/complete";
    }

    private void setAuthenticatedSession(Authentication authentication, HttpServletRequest request, HttpServletResponse response) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);
    }

    // 소셜 로그인으로 처음 들어와 아직 DB에 저장되지 않은(id == null) 임시 사용자라면 반환한다.
    private User getPendingSocialUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof OAuth2AuthenticationToken oAuth2Token
                && oAuth2Token.getPrincipal() instanceof CustomOAuth2User customOAuth2User
                && customOAuth2User.getUser().getId() == null) {
            return customOAuth2User.getUser();
        }
        return null;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
