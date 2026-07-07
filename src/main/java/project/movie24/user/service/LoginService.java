package project.movie24.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final AuthenticationManager authenticationManager;

    /**
     * @return null이면 로그인 실패
     */
    public Authentication login(String loginId, String password) {
        try {
            return authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginId, password));
        } catch (AuthenticationException e) {
            return null;
        }
    }
}
