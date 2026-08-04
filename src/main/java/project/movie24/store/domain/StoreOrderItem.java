package project.movie24.store.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 주문 시점의 상품명/단가를 스냅샷으로 남겨, 이후 StoreItem이 수정/삭제돼도 과거 주문 내역이 바뀌지 않게 한다.
 */
@Entity
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class StoreOrderItem {

    @Id @GeneratedValue
    @Column(name = "store_order_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_order_id")
    private StoreOrder storeOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_item_id")
    private StoreItem storeItem;

    private String itemName;
    private Integer unitPrice;
    private Integer quantity;

    public int lineTotal() {
        return unitPrice * quantity;
    }
}
