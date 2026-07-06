package project.movie24.theater.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class TheaterRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String region;

    private String address;
}
