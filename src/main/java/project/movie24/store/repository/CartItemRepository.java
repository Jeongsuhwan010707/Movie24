package project.movie24.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import project.movie24.store.domain.CartItem;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByUserIdAndStoreItemId(Long userId, Long storeItemId);

    // 장바구니 화면에서 상품 정보를 항목마다 따로 조회하지 않도록 한 번에 조인해온다.
    @Query("select c from CartItem c join fetch c.storeItem where c.user.id = :userId order by c.createdAt")
    List<CartItem> findByUserIdWithStoreItem(@Param("userId") Long userId);

    @Query("select c from CartItem c join fetch c.storeItem where c.id in :cartItemIds and c.user.id = :userId")
    List<CartItem> findByIdInAndUserId(@Param("cartItemIds") List<Long> cartItemIds, @Param("userId") Long userId);

    long countByUserId(Long userId);
}
