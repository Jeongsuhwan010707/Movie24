package project.movie24.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import project.movie24.store.domain.StoreOrder;

import java.util.List;

public interface StoreOrderRepository extends JpaRepository<StoreOrder, Long> {

    @Query("select o from StoreOrder o where o.user.id = :userId order by o.orderedAt desc")
    List<StoreOrder> findByUserIdOrderByOrderedAtDesc(@Param("userId") Long userId);
}
