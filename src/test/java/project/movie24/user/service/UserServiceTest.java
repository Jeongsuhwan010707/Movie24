package project.movie24.user.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import project.movie24.user.domain.EmailStatus;
import project.movie24.user.domain.Grade;
import project.movie24.user.domain.User;
import project.movie24.user.repository.UserRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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

    @ParameterizedTest
    @CsvSource({
            "199999, NORMAL",
            "200000, SILVER",
            "499999, SILVER",
            "500000, GOLD",
            "999999, GOLD",
            "1000000, VIP",
    })
    void recalculateGrade_upgradesToThresholdMatchingGrade(long recentSpend, Grade expectedGrade) {
        UserService userService = new UserService(userRepository, passwordEncoder);
        User user = User.builder().id(1L).grade(Grade.NORMAL).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.recalculateGrade(1L, recentSpend);

        assertThat(result.getGrade()).isEqualTo(expectedGrade);
    }

    @Test
    void recalculateGrade_neverDowngradesExistingGrade() {
        UserService userService = new UserService(userRepository, passwordEncoder);
        User user = User.builder().id(1L).grade(Grade.VIP).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = userService.recalculateGrade(1L, 0L);

        assertThat(result.getGrade()).isEqualTo(Grade.VIP);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void recalculateGrade_skipsAdminAccounts() {
        UserService userService = new UserService(userRepository, passwordEncoder);
        User admin = User.builder().id(1L).grade(Grade.ADMIN).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));

        User result = userService.recalculateGrade(1L, 10_000_000L);

        assertThat(result.getGrade()).isEqualTo(Grade.ADMIN);
        verify(userRepository, never()).save(any(User.class));
    }

    @ParameterizedTest
    @CsvSource({
            "NORMAL, 0",
            "SILVER, 5",
            "GOLD, 10",
            "VIP, 20",
            "ADMIN, 0",
    })
    void discountRateFor_returnsRatePerGrade(Grade grade, int expectedRate) {
        UserService userService = new UserService(userRepository, passwordEncoder);

        assertThat(userService.discountRateFor(grade)).isEqualTo(expectedRate);
    }
}
