package project.movie24.user.domain;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class ResetPasswordForm {
    @NotEmpty
    private String newPassword;
    @NotEmpty
    private String newPasswordConfirm;
}
