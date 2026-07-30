package project.movie24.user.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.movie24.user.domain.Provider;
import project.movie24.user.domain.User;
import project.movie24.user.dto.FindIdRequest;
import project.movie24.user.dto.FindIdResponse;
import project.movie24.user.dto.FindPasswordRequest;
import project.movie24.user.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class FindAccountApiController {

    private static final Map<Provider, String> PROVIDER_LABELS = Map.of(
            Provider.KAKAO, "카카오",
            Provider.NAVER, "네이버",
            Provider.GOOGLE, "구글"
    );

    private final UserService userService;

    @PostMapping("/find-id")
    public ResponseEntity<FindIdResponse> findId(@Valid @RequestBody FindIdRequest request) {
        List<User> matches = userService.findByNameAndEmail(request.getName(), request.getEmail());
        if (matches.isEmpty()) {
            throw new IllegalArgumentException("일치하는 회원 정보가 없습니다.");
        }

        // 같은 이름+이메일로 일반 가입 + 여러 소셜 계정을 동시에 갖고 있을 수 있으므로 모두 보여준다.
        List<String> maskedLoginIds = matches.stream()
                .filter(u -> u.getProvider() == Provider.LOCAL)
                .map(u -> maskLoginId(u.getLoginId()))
                .distinct()
                .toList();
        List<String> socialLabels = socialProviderLabels(matches);

        return ResponseEntity.ok(FindIdResponse.builder()
                .maskedLoginIds(maskedLoginIds)
                .socialProviders(socialLabels)
                .build());
    }

    @PostMapping("/find-password/verify")
    public ResponseEntity<Void> verifyForPasswordReset(@Valid @RequestBody FindPasswordRequest request,
                                                         HttpServletRequest httpRequest) {
        Optional<User> verified = userService.verifyLocalAccount(
                request.getLoginId(), request.getName(), request.getEmail());
        if (verified.isEmpty()) {
            throw new IllegalArgumentException(resetGuidance(request.getName(), request.getEmail()));
        }

        PasswordResetSession.markVerified(httpRequest, verified.get().getId());
        return ResponseEntity.ok().build();
    }

    /**
     * 입력한 아이디+이름+이메일로 로컬 계정을 특정하지 못했을 때, 같은 이름+이메일의 다른 계정이
     * 있는지(다른 아이디의 로컬 계정, 또는 소셜 계정 - 여러 개일 수 있음) 확인해 안내 문구를 만든다.
     */
    private String resetGuidance(String name, String email) {
        List<User> matches = userService.findByNameAndEmail(name, email);
        if (matches.isEmpty()) {
            return "일치하는 회원 정보가 없습니다.";
        }

        List<String> messages = new ArrayList<>();
        boolean hasLocal = matches.stream().anyMatch(u -> u.getProvider() == Provider.LOCAL);
        if (hasLocal) {
            messages.add("입력하신 아이디가 일치하지 않습니다. 아이디 찾기로 다시 확인해주세요.");
        }
        List<String> socialLabels = socialProviderLabels(matches);
        if (!socialLabels.isEmpty()) {
            String labels = String.join(", ", socialLabels);
            messages.add(labels + " 계정으로 가입되어 있습니다. 해당 소셜 로그인을 이용해주세요.");
        }
        return String.join(" ", messages);
    }

    private List<String> socialProviderLabels(List<User> users) {
        return users.stream()
                .map(User::getProvider)
                .filter(p -> p != Provider.LOCAL)
                .map(PROVIDER_LABELS::get)
                .distinct()
                .toList();
    }

    // 끝쪽 2~3자만 가리고 나머지는 그대로 보여준다(짧은 아이디는 최소 1자만 가림).
    private String maskLoginId(String loginId) {
        int length = loginId.length();
        int maskCount = Math.min(length - 1, length <= 3 ? 1 : (length <= 5 ? 2 : 3));
        int visibleCount = length - maskCount;
        return loginId.substring(0, visibleCount) + "*".repeat(maskCount);
    }
}
