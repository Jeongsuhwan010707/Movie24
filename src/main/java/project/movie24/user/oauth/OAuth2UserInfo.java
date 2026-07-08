package project.movie24.user.oauth;

public interface OAuth2UserInfo {

    String getProviderId();

    String getNickname();

    String getEmail();
}
