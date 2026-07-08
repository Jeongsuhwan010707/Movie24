package project.movie24.user.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UserForm {

    private String loginId;
    private String password;
    private String name;
    private String nickName;
    private String address;
    private String phone;
    private String email;
    private String emailYn;
    private EmailStatus emailStatus;
    private String gender;
    private LocalDate birthDate;

}