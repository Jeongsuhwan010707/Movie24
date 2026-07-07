package project.movie24.user.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import project.movie24.user.domain.EmailStatus;
import project.movie24.user.domain.User;
import project.movie24.user.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void saveUser_encodesPasswordBeforeSaving() {
        UserService userService = new UserService(userRepository, passwordEncoder);
        User rawUser = User.builder()
                .loginId("tester")
                .password("raw-password")
                .name("테스터")
                .emailStatus(EmailStatus.ALLOW)
                .build();
        when(passwordEncoder.encode("raw-password")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User saved = userService.saveUser(rawUser);

        assertThat(saved.getPassword()).isEqualTo("encoded-password");
        verify(userRepository).save(any(User.class));
    }
}
