package project.movie24.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import project.movie24.user.domain.CustomOAuth2User;
import project.movie24.user.domain.EmailStatus;
import project.movie24.user.domain.Provider;
import project.movie24.user.domain.User;
import project.movie24.user.oauth.OAuth2UserInfo;
import project.movie24.user.oauth.OAuth2UserInfoFactory;
import project.movie24.user.repository.UserRepository;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.of(registrationId, oAuth2User.getAttributes());
        Provider provider = Provider.valueOf(registrationId.toUpperCase());

        User user = userRepository.findByProviderAndProviderId(provider, userInfo.getProviderId())
                .orElseGet(() -> userRepository.save(User.builder()
                        .loginId(registrationId + "_" + userInfo.getProviderId())
                        .name(userInfo.getName() != null ? userInfo.getName() : userInfo.getNickname())
                        .nickName(userInfo.getNickname())
                        .email(userInfo.getEmail())
                        .phone(userInfo.getPhone())
                        .gender(userInfo.getGender())
                        .birthDate(parseBirthDate(userInfo))
                        .emailStatus(EmailStatus.ALLOW)
                        .provider(provider)
                        .providerId(userInfo.getProviderId())
                        .build()));

        return new CustomOAuth2User(user, oAuth2User.getAttributes());
    }

    // 생일(MM-DD)+출생연도(yyyy)를 둘 다 동의받은 경우에만 조합 가능 - 하나라도 없으면 null
    private LocalDate parseBirthDate(OAuth2UserInfo userInfo) {
        if (userInfo.getBirthyear() == null || userInfo.getBirthday() == null) {
            return null;
        }
        try {
            return LocalDate.parse(userInfo.getBirthyear() + "-" + userInfo.getBirthday());
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
