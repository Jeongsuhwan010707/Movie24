package project.movie24.store.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CheckoutPrepareResponse {

    private String orderId;
    private String orderName;
    private int amount;
}
