package project.movie24.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.movie24.store.domain.StoreOrderItem;

import java.util.List;

public interface StoreOrderItemRepository extends JpaRepository<StoreOrderItem, Long> {

    List<StoreOrderItem> findByStoreOrderId(Long storeOrderId);

    List<StoreOrderItem> findByStoreOrderIdIn(List<Long> storeOrderIds);
}
