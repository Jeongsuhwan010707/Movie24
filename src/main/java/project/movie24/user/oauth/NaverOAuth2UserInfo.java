package project.movie24.user.oauth;

import java.util.Map;

public class NaverOAuth2UserInfo implements OAuth2UserInfo {

    private final Map<String, Object> response;

    @SuppressWarnings("unchecked")
    public NaverOAuth2UserInfo(Map<String, Object> attributes) {
        this.response = (Map<String, Object>) attributes.getOrDefault("response", Map.of());
    }

    @Override
    public String getProviderId() {
        return String.valueOf(response.get("id"));
    }

    @Override
    public String getNickname() {
        return (String) response.get("nickname");
    }

    @Override
    public String getEmail() {
        return (String) response.get("email");
    }

    @Override
    public String getName() {
        return (String) response.get("name");
    }

    @Override
    public String getPhone() {
        return (String) response.get("mobile");
    }

    @Override
    public String getGender() {
        return (String) response.get("gender");
    }

    @Override
    public String getBirthday() {
        return (String) response.get("birthday");
    }

    @Override
    public String getBirthyear() {
        return (String) response.get("birthyear");
    }
}
