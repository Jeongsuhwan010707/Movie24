package project.movie24.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import project.movie24.user.domain.EmailStatus;
import project.movie24.user.domain.User;
import project.movie24.user.domain.UserPrincipal;
import project.movie24.user.dto.SignupRequest;
import project.movie24.user.dto.UserResponse;
import project.movie24.user.service.UserService;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserApiController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(UserResponse.from(principal.getUser()));
    }

    @GetMapping("/check-id")
    public ResponseEntity<Map<String, Boolean>> checkId(@RequestParam String loginId) {
        return ResponseEntity.ok(Map.of("available", userService.isLoginIdAvailable(loginId)));
    }

    @PostMapping
    public ResponseEntity<UserResponse> signup(@Valid @RequestBody SignupRequest request) {
        User user = User.builder()
                .loginId(request.getLoginId())
                .password(request.getPassword())
                .name(request.getName())
                .nickName(request.getNickName())
                .address(request.getAddress())
                .phone(request.getPhone())
                .email(request.getEmail())
                .emailStatus("Y".equals(request.getEmailYn()) ? EmailStatus.ALLOW : EmailStatus.REJECT)
                .build();

        User saved = userService.saveUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(saved));
    }
}
