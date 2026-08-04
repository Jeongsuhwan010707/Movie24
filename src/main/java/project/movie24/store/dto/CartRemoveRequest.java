package project.movie24.store.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;

import java.util.List;

@Getter
public class CartRemoveRequest {

    @NotEmpty
    private List<Long> cartItemIds;
}
