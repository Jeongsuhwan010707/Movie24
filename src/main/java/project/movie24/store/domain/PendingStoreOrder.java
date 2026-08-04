package project.movie24.store.domain;

import lombok.Getter;

import java.io.Serializable;
import java.util.List;

@Getter
public class PendingStoreOrder implements Serializable {

    private final String orderId;
    private final List<Long> cartItemIds;
    private final int amount;

    public PendingStoreOrder(String orderId, List<Long> cartItemIds, int amount) {
        this.orderId = orderId;
        this.cartItemIds = cartItemIds;
        this.amount = amount;
    }
}
