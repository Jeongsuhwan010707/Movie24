package project.movie24.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;
import project.movie24.user.domain.CustomOAuth2User;

import java.io.IOException;

/**
 * 소셜 로그인은 됐지만 아직 /users/new에서 추가 정보 입력을 마치지 않은(DB 미저장, id == null)
 * 사용자가 다른 페이지로 진입하면 입력 화면으로 돌려보낸다.
 */
public class PendingSocialSignupFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String uri = request.getRequestURI();
        boolean allowed = uri.startsWith("/users/") || uri.startsWith("/resources/")
                || uri.startsWith("/oauth2/") || uri.startsWith("/login")
                || uri.equals("/logout") || uri.startsWith("/error") || uri.endsWith(".ico");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!allowed && auth instanceof OAuth2AuthenticationToken token
                && token.getPrincipal() instanceof CustomOAuth2User customOAuth2User
                && customOAuth2User.getUser().getId() == null) {
            response.sendRedirect(request.getContextPath() + "/users/new");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
