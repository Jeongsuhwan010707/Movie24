package project.movie24.mypage.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileUpdateForm {
    private String address;
    private String phone;
    private String email;
    private String emailYn;
}
