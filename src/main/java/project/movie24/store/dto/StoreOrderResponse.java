package project.movie24.store.dto;

import lombok.Builder;
import lombok.Getter;
import project.movie24.store.domain.StoreOrder;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class StoreOrderResponse {

    private Long id;
    private String orderUid;
    private Integer totalAmount;
    private LocalDateTime orderedAt;
    private List<StoreOrderItemResponse> items;
    private String entryCode;

    public static StoreOrderResponse from(StoreOrder order, List<StoreOrderItemResponse> items) {
        return StoreOrderResponse.builder()
                .id(order.getId())
                .orderUid(order.getOrderUid())
                .totalAmount(order.getTotalAmount())
                .orderedAt(order.getOrderedAt())
                .items(items)
                .entryCode(order.getEntryCode())
                .build();
    }
}
