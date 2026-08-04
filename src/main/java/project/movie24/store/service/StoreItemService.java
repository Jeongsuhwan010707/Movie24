package project.movie24.store.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.movie24.common.EntityFinders;
import project.movie24.store.domain.StoreCategory;
import project.movie24.store.domain.StoreItem;
import project.movie24.store.dto.StoreItemRequest;
import project.movie24.store.repository.StoreItemRepository;

import java.util.Comparator;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class StoreItemService {

    private final StoreItemRepository storeItemRepository;

    public StoreItem register(StoreItemRequest request) {
        StoreItem item = StoreItem.builder()
                .category(request.getCategory())
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .imageUrl(request.getImageUrl())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .build();
        return storeItemRepository.save(item);
    }

    public StoreItem update(Long storeItemId, StoreItemRequest request) {
        StoreItem item = getOrThrow(storeItemId);
        item.update(request.getCategory(), request.getName(), request.getDescription(), request.getPrice(),
                request.getImageUrl(), request.getDisplayOrder() != null ? request.getDisplayOrder() : 0);
        return item;
    }

    public void delete(Long storeItemId) {
        storeItemRepository.delete(getOrThrow(storeItemId));
    }

    @Transactional(readOnly = true)
    public StoreItem findOne(Long storeItemId) {
        return getOrThrow(storeItemId);
    }

    @Transactional(readOnly = true)
    public List<StoreItem> findAll() {
        return storeItemRepository.findAll().stream()
                .sorted(Comparator.comparing(StoreItem::getDisplayOrder))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StoreItem> findByCategory(StoreCategory category) {
        return storeItemRepository.findByCategory(category).stream()
                .sorted(Comparator.comparing(StoreItem::getDisplayOrder))
                .toList();
    }

    private StoreItem getOrThrow(Long storeItemId) {
        return EntityFinders.getOrThrow(storeItemRepository, storeItemId, "스토어 상품");
    }
}
