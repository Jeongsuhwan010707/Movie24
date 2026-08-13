package project.movie24.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.movie24.user.domain.EmailStatus;
import project.movie24.user.domain.Grade;
import project.movie24.user.domain.Provider;
import project.movie24.user.domain.User;
import project.movie24.user.dto.GradeTierResponse;
import project.movie24.user.repository.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    // 최근 1년 누적 결제금액 기준 VIP 등급 승급 임계값. 강등은 하지 않는다.
    private static final int SILVER_THRESHOLD = 200_000;
    private static final int GOLD_THRESHOLD = 500_000;
    private static final int VIP_THRESHOLD = 1_000_000;

    // 등급별 월 1회 결제 할인율(%). 결제 총액에 곱해 원 단위로 내림한다.
    private static final Map<Grade, Integer> DISCOUNT_RATE_BY_GRADE = Map.of(
            Grade.NORMAL, 0, Grade.SILVER, 5, Grade.GOLD, 10, Grade.VIP, 20);

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

    public User updateProfile(Long userId, String address, String phone, String email, EmailStatus emailStatus) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        User updated = user.toBuilder()
                .address(address)
                .phone(phone)
                .email(email)
                .emailStatus(emailStatus)
                .build();
        return userRepository.save(updated);
    }

    public User updateNickName(Long userId, String nickName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        User updated = user.toBuilder().nickName(nickName).build();
        return userRepository.save(updated);
    }

    /**
     * 최근 1년 누적 결제금액을 바탕으로 등급을 재계산한다. ADMIN은 대상에서 제외하고,
     * 이미 산정된 등급보다 낮은 등급으로는 절대 강등하지 않는다(승급만 수행).
     */
    public User recalculateGrade(Long userId, long recentSpend) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        if (user.getGrade() == Grade.ADMIN) {
            return user;
        }

        Grade target = gradeFor(recentSpend);
        if (target.ordinal() <= user.getGrade().ordinal()) {
            return user;
        }
        return userRepository.save(user.toBuilder().grade(target).build());
    }

    /**
     * 마이페이지에서 "다음 등급까지 남은 금액"을 보여주기 위한 다음 등급 임계값.
     * VIP/ADMIN은 더 오를 등급이 없으므로 null을 반환한다.
     */
    public Integer nextGradeThreshold(Grade grade) {
        return switch (grade) {
            case NORMAL -> SILVER_THRESHOLD;
            case SILVER -> GOLD_THRESHOLD;
            case GOLD -> VIP_THRESHOLD;
            default -> null;
        };
    }

    /**
     * 등급별 월 1회 결제 할인율(%). 정의되지 않은 등급(ADMIN 등)은 0을 반환한다.
     */
    public int discountRateFor(Grade grade) {
        return DISCOUNT_RATE_BY_GRADE.getOrDefault(grade, 0);
    }

    /**
     * 마이페이지 "등급 혜택 안내"에서 등급별 할인율/승급 기준을 표로 보여주기 위한 목록.
     */
    public List<GradeTierResponse> gradeTiers() {
        return List.of(
                GradeTierResponse.builder().grade(Grade.NORMAL).discountRate(discountRateFor(Grade.NORMAL)).minSpend(0).build(),
                GradeTierResponse.builder().grade(Grade.SILVER).discountRate(discountRateFor(Grade.SILVER)).minSpend(SILVER_THRESHOLD).build(),
                GradeTierResponse.builder().grade(Grade.GOLD).discountRate(discountRateFor(Grade.GOLD)).minSpend(GOLD_THRESHOLD).build(),
                GradeTierResponse.builder().grade(Grade.VIP).discountRate(discountRateFor(Grade.VIP)).minSpend(VIP_THRESHOLD).build()
        );
    }

    private Grade gradeFor(long recentSpend) {
        if (recentSpend >= VIP_THRESHOLD) {
            return Grade.VIP;
        }
        if (recentSpend >= GOLD_THRESHOLD) {
            return Grade.GOLD;
        }
        if (recentSpend >= SILVER_THRESHOLD) {
            return Grade.SILVER;
        }
        return Grade.NORMAL;
    }
}
