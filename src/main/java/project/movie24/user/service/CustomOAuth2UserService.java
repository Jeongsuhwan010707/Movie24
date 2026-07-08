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
                        .name(userInfo.getNickname())
                        .nickName(userInfo.getNickname())
                        .email(userInfo.getEmail())
                        .emailStatus(EmailStatus.ALLOW)
                        .provider(provider)
                        .providerId(userInfo.getProviderId())
                        .build()));

        return new CustomOAuth2User(user, oAuth2User.getAttributes());
    }
}
