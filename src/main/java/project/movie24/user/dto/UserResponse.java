package project.movie24.user.dto;

import lombok.Builder;
import lombok.Getter;
import project.movie24.user.domain.User;

@Getter
@Builder
public class UserResponse {

    private Long id;
    private String loginId;
    private String name;
    private String nickName;
    private String email;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .loginId(user.getLoginId())
                .name(user.getName())
                .nickName(user.getNickName())
                .email(user.getEmail())
                .build();
    }
}
