package project.movie24.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.movie24.store.domain.StoreCategory;
import project.movie24.store.domain.StoreItem;

import java.util.List;

public interface StoreItemRepository extends JpaRepository<StoreItem, Long> {

    List<StoreItem> findByCategory(StoreCategory category);
}
