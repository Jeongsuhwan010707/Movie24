package project.movie24.store.dto;

import lombok.Builder;
import lombok.Getter;
import project.movie24.store.domain.StoreCategory;
import project.movie24.store.domain.StoreItem;

@Getter
@Builder
public class StoreItemResponse {

    private Long id;
    private StoreCategory category;
    private String name;
    private String description;
    private Integer price;
    private String imageUrl;
    private Integer displayOrder;

    public static StoreItemResponse from(StoreItem item) {
        return StoreItemResponse.builder()
                .id(item.getId())
                .category(item.getCategory())
                .name(item.getName())
                .description(item.getDescription())
                .price(item.getPrice())
                .imageUrl(item.getImageUrl())
                .displayOrder(item.getDisplayOrder())
                .build();
    }
}
