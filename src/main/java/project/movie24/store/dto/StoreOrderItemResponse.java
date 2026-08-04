package project.movie24.store.dto;

import lombok.Builder;
import lombok.Getter;
import project.movie24.store.domain.StoreOrderItem;

@Getter
@Builder
public class StoreOrderItemResponse {

    private String itemName;
    private Integer unitPrice;
    private Integer quantity;
    private Integer lineTotal;

    public static StoreOrderItemResponse from(StoreOrderItem item) {
        return StoreOrderItemResponse.builder()
                .itemName(item.getItemName())
                .unitPrice(item.getUnitPrice())
                .quantity(item.getQuantity())
                .lineTotal(item.lineTotal())
                .build();
    }
}
