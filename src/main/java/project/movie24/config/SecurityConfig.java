package project.movie24.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 아직 브라우저 프론트가 없는 API 서버 단계라 CSRF는 비활성화한다.
                // 세션 쿠키를 쓰는 브라우저 프론트가 붙는 시점에 다시 검토한다.
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", "/users/**", "/login", "/logout",
                                "/api/users", "/api/login", "/api/logout",
                                "/resources/**", "/*.ico", "/error"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                // 로그인은 LoginController(뷰)/AuthApiController(REST)가 AuthenticationManager로 직접 처리한다.
                .formLogin(form -> form.disable())
                .logout(logout -> logout
                        .logoutRequestMatcher(new OrRequestMatcher(
                                new AntPathRequestMatcher("/logout", "POST"),
                                new AntPathRequestMatcher("/api/logout", "POST")
                        ))
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        // 뷰 기반 로그아웃(/logout)은 리다이렉트, REST 로그아웃(/api/logout)은 상태코드만 반환
                        .logoutSuccessHandler((request, response, authentication) -> {
                            if (request.getRequestURI().startsWith("/api/")) {
                                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                            } else {
                                response.sendRedirect("/");
                            }
                        })
                );
        return http.build();
    }
}
