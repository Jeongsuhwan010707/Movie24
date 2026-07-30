package project.movie24.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.movie24.user.domain.Provider;
import project.movie24.user.domain.User;
import project.movie24.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User saveUser(User user) {
        // 소셜 회원가입은 비밀번호가 없으므로(null) 인코딩을 건너뛴다.
        User userToSave = user.getPassword() == null
                ? user
                : user.toBuilder().password(passwordEncoder.encode(user.getPassword())).build();
        return userRepository.save(userToSave);
    }

    public boolean isLoginIdAvailable(String loginId) {
        return !userRepository.existsByLoginId(loginId);
    }

    /**
     * 아이디 + 이름 + 이메일이 모두 일치하는 로컬 계정인지 확인한다(비밀번호 재설정 전 본인확인).
     * loginId가 유니크 컬럼이라 결과는 항상 0~1건이다.
     */
    public Optional<User> verifyLocalAccount(String loginId, String name, String email) {
        return userRepository.findByLoginIdAndNameAndEmailAndProvider(loginId, name, email, Provider.LOCAL);
    }

    /**
     * 이름 + 이메일이 일치하는 모든 계정(일반 + 소셜)을 찾는다. email은 유니크 컬럼이 아니고,
     * 같은 사람이 일반 가입 + 여러 소셜 계정을 동시에 가질 수도 있으므로 여러 건이 나올 수 있다.
     */
    public List<User> findByNameAndEmail(String name, String email) {
        return userRepository.findAllByNameAndEmail(name, email);
    }

    public void resetPassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        userRepository.save(user.toBuilder().password(passwordEncoder.encode(newPassword)).build());
    }
}
