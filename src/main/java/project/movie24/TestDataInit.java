package project.movie24;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import project.movie24.user.domain.EmailStatus;
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
        if (userRepository.findByLoginId("test").isPresent()) {
            return;
        }

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
}
