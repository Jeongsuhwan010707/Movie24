package project.movie24.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class FindIdRequest {
    @NotBlank
    private String name;
    @NotBlank
    private String email;
}
