package project.movie24.screen.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

@Getter
public class ScreenRequest {

    @NotNull
    private Long theaterId;

    @NotBlank
    private String name;

    @Positive
    private Integer totalSeats;

    @NotBlank
    private String screenType;
}
