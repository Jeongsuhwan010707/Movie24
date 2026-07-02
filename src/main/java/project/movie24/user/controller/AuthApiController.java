package project.movie24.user.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import project.movie24.user.domain.User;
import project.movie24.user.domain.UserPrincipal;
import project.movie24.user.dto.LoginRequest;
import project.movie24.user.dto.UserResponse;
import project.movie24.user.service.LoginService;

@RestController
@RequiredArgsConstructor
public class AuthApiController {

    private final LoginService loginService;
    private final SecurityContextRepository securityContextRepository;

    @PostMapping("/api/login")
    public ResponseEntity<UserResponse> login(@Valid @RequestBody LoginRequest request,
                                               HttpServletRequest httpRequest,
                                               HttpServletResponse httpResponse) {
        Authentication authentication = loginService.login(request.getLoginId(), request.getPassword());
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);

        User user = ((UserPrincipal) authentication.getPrincipal()).getUser();
        return ResponseEntity.ok(UserResponse.from(user));
    }
}
