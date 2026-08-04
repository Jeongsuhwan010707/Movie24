package project.movie24.store.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import project.movie24.store.domain.StoreCategory;

@Getter
public class StoreItemRequest {

    @NotNull
    private StoreCategory category;

    @NotBlank
    private String name;

    private String description;

    @NotNull
    @Positive
    private Integer price;

    private String imageUrl;

    private Integer displayOrder;
}
