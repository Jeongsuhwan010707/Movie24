package project.movie24.store.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

@Getter
public class CartQuantityRequest {

    @NotNull
    @Positive
    private Integer quantity;
}
