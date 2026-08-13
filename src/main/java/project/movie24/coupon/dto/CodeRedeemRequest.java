package project.movie24.coupon.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class CodeRedeemRequest {

    @NotBlank
    private String code;
}
