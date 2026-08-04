package project.movie24.store.dto;

import lombok.Builder;
import lombok.Getter;
import project.movie24.store.domain.CartItem;
import project.movie24.store.domain.StoreCategory;
import project.movie24.store.domain.StoreItem;

@Getter
@Builder
public class CartItemResponse {

    private Long id;
    private Long storeItemId;
    private StoreCategory category;
    private String name;
    private String description;
    private String imageUrl;
    private Integer unitPrice;
    private Integer quantity;
    private Integer lineTotal;

    public static CartItemResponse from(CartItem cartItem) {
        StoreItem storeItem = cartItem.getStoreItem();
        return CartItemResponse.builder()
                .id(cartItem.getId())
                .storeItemId(storeItem.getId())
                .category(storeItem.getCategory())
                .name(storeItem.getName())
                .description(storeItem.getDescription())
                .imageUrl(storeItem.getImageUrl())
                .unitPrice(storeItem.getPrice())
                .quantity(cartItem.getQuantity())
                .lineTotal(storeItem.getPrice() * cartItem.getQuantity())
                .build();
    }
}
