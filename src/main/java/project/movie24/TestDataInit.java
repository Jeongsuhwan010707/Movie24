package project.movie24;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import project.movie24.user.domain.EmailStatus;
import project.movie24.user.domain.Grade;
import project.movie24.user.domain.User;
import project.movie24.user.repository.UserRepository;

@Component
@RequiredArgsConstructor
public class TestDataInit {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        if (userRepository.findByLoginId("test").isEmpty()) {
            User user = User.builder()
                    .loginId("test")
                    .password(passwordEncoder.encode("test!"))
                    .name("테스트")
                    .nickName("닉네임")
                    .address("서울시 중구 111")
                    .email("test@naver.com")
                    .emailStatus(EmailStatus.ALLOW)
                    .build();
            userRepository.save(user);
        }

        // 영화/상영관/상영시간/좌석 관리 API가 ADMIN 등급만 호출 가능하도록 잠겨있어,
        // 관리자 화면이 생기기 전까지 로컬에서 데이터 등록/수정용으로 쓸 계정.
        if (userRepository.findByLoginId("admin").isEmpty()) {
            User admin = User.builder()
                    .loginId("admin")
                    .password(passwordEncoder.encode("admin!"))
                    .name("관리자")
                    .nickName("관리자")
                    .address("서울시 중구 111")
                    .email("admin@naver.com")
                    .emailStatus(EmailStatus.ALLOW)
                    .grade(Grade.ADMIN)
                    .build();
            userRepository.save(admin);
        }
    }
}
