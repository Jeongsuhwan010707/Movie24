package project.movie24.user.oauth;

public interface OAuth2UserInfo {

    String getProviderId();

    String getNickname();

    String getEmail();

    // 카카오/구글은 제공하지 않아서 기본값 null
    default String getName() {
        return null;
    }

    default String getPhone() {
        return null;
    }

    default String getGender() {
        return null;
    }

    default String getBirthday() {
        return null;
    }

    default String getBirthyear() {
        return null;
    }
}
