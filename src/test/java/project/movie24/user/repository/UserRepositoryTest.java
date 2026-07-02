package project.movie24.user.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import project.movie24.user.domain.EmailStatus;
import project.movie24.user.domain.User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByLoginId_returnsSavedUser() {
        userRepository.save(User.builder()
                .loginId("tester")
                .password("encoded-password")
                .name("테스터")
                .emailStatus(EmailStatus.ALLOW)
                .build());

        assertThat(userRepository.findByLoginId("tester")).isPresent();
        assertThat(userRepository.findByLoginId("no-such-id")).isEmpty();
    }

    @Test
    void loginId_mustBeUnique() {
        userRepository.saveAndFlush(User.builder()
                .loginId("dup")
                .password("pw")
                .name("A")
                .emailStatus(EmailStatus.ALLOW)
                .build());

        assertThatThrownBy(() -> userRepository.saveAndFlush(User.builder()
                .loginId("dup")
                .password("pw2")
                .name("B")
                .emailStatus(EmailStatus.ALLOW)
                .build()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
