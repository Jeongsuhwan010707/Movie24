package project.movie24.config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import project.movie24.user.domain.CustomOAuth2User;
import project.movie24.user.service.CustomOAuth2UserService;

@Slf4j
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
    public SecurityFilterChain filterChain(HttpSecurity http, CustomOAuth2UserService customOAuth2UserService) throws Exception {
        http
                // 세션 쿠키 기반 인증(뷰 로그인 + /api 로그인 공용)이라 CSRF는 기본 설정(세션에 토큰 저장)으로 켜둔다.
                // 타임리프 폼에는 _csrf 히든 필드를 직접 추가했다. 이후 /api를 JS에서 fetch로 호출하게 되면
                // 토큰을 meta 태그 등으로 노출해 헤더에 실어 보내야 한다.
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", "/users/**", "/login", "/logout",
                                "/api/users", "/api/users/check-id", "/api/login", "/api/logout",
                                "/api/users/find-id", "/api/users/find-password/verify",
                                "/resources/**", "/*.ico", "/error",
                                "/help", "/help/**", "/store",
                                "/movies", "/movies/**",
                                "/theaters/nearby",
                                "/events", "/events/**",
                                "/movieReservation/**", "/main/**",
                                "/oauth2/**", "/login/oauth2/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/api/movies", "/api/movies/**",
                                "/api/theaters", "/api/theaters/**",
                                "/api/screens", "/api/screens/**",
                                "/api/showtimes", "/api/showtimes/**",
                                "/api/seats", "/api/seats/**",
                                "/api/reservations/showtimes/**",
                                "/api/store/items", "/api/store/items/**",
                                "/api/events", "/api/events/**"
                        ).permitAll()
                        // 영화/상영관/상영시간/좌석/스토어 상품/이벤트 데이터 관리는 아직 전용 관리자 화면이 없어 API를 직접 호출해 쓰지만,
                        // 조회(GET) 외 등록/수정/삭제는 ADMIN 등급만 가능하도록 잠가둔다.
                        .requestMatchers(
                                "/api/movies", "/api/movies/**",
                                "/api/theaters", "/api/theaters/**",
                                "/api/screens", "/api/screens/**",
                                "/api/showtimes", "/api/showtimes/**",
                                "/api/seats", "/api/seats/**",
                                "/api/store/items", "/api/store/items/**",
                                "/api/events", "/api/events/**"
                        ).hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                // oauth2Login().loginPage("/login")이 전체 필터체인의 기본 AuthenticationEntryPoint가 되어버려서,
                // 이게 없으면 /api/** 요청도 인증 실패 시 401이 아니라 /login으로 302 리다이렉트된다.
                // fetch는 리다이렉트를 그대로 따라가 최종적으로 200(로그인 페이지 HTML)을 받아버리므로,
                // JS에서 res.status로 로그인 여부를 판단하는 코드(예: 로그인 모달 게이트)가 전부 무력화된다.
                // /api/**만 순수 401로 응답하도록 별도 entry point를 지정해 분리한다.
                // 같은 이유로 인가 실패(ADMIN 권한 없음)도 /login으로 리다이렉트되지 않고 순수 403으로 응답하게 한다.
                .exceptionHandling(exceptions -> exceptions
                        .defaultAuthenticationEntryPointFor(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                new AntPathRequestMatcher("/api/**")
                        )
                        .defaultAccessDeniedHandlerFor(
                                (request, response, e) -> response.setStatus(HttpStatus.FORBIDDEN.value()),
                                new AntPathRequestMatcher("/api/**")
                        )
                )
                // 로그인은 LoginController(뷰)/AuthApiController(REST)가 AuthenticationManager로 직접 처리한다.
                .formLogin(form -> form.disable())
                // 소셜 로그인(카카오 등)은 OAuth2LoginAuthenticationFilter가 처리하고,
                // 성공 시 securityContextRepository()에 자동으로 세션 저장된다.
                .oauth2Login(oauth2 -> oauth2
                        // loginPage를 지정하지 않으면 스프링 시큐리티가 /login에 자체 로그인 페이지를 자동 생성해
                        // LoginController의 커스텀 로그인 화면을 가려버린다.
                        .loginPage("/login")
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        // 처음 연동하는 소셜 계정(아직 DB에 저장 안 됨, user.id == null)은 추가 정보 입력을 위해
                        // /users/new로 보내고, 이미 가입된 계정은 바로 메인으로 보낸다.
                        .successHandler((request, response, authentication) -> {
                            if (authentication.getPrincipal() instanceof CustomOAuth2User customOAuth2User
                                    && customOAuth2User.getUser().getId() == null) {
                                response.sendRedirect("/users/new");
                            } else {
                                response.sendRedirect("/");
                            }
                        })
                        // 실패 원인이 /login?error 리다이렉트에 묻혀 콘솔에 안 보이길래 임시로 로그를 남긴다.
                        .failureHandler((request, response, exception) -> {
                            log.error("OAuth2 로그인 실패", exception);
                            response.sendRedirect("/login?error");
                        })
                )
                .logout(logout -> logout
                        .logoutRequestMatcher(new OrRequestMatcher(
                                new AntPathRequestMatcher("/logout", "POST"),
                                new AntPathRequestMatcher("/api/logout", "POST")
                        ))
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        // 뷰 기반 로그아웃(/logout)은 리다이렉트, REST 로그아웃(/api/logout)은 상태코드만 반환(프론트에서 알아서 처리
                        .logoutSuccessHandler((request, response, authentication) -> {
                            if (request.getRequestURI().startsWith("/api/")) {
                                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                            } else {
                                response.sendRedirect("/");
                            }
                        })
                )
                .addFilterBefore(new PendingSocialSignupFilter(), AuthorizationFilter.class);
        return http.build();
    }
}
