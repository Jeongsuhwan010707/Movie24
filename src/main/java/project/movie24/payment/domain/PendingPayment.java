package project.movie24.payment.domain;

import lombok.Getter;

import java.io.Serializable;
import java.util.List;

@Getter
public class PendingPayment implements Serializable {

    private final String orderId;
    private final Long showtimeId;
    private final List<Long> seatIds;
    private final int amount;

    public PendingPayment(String orderId, Long showtimeId, List<Long> seatIds, int amount) {
        this.orderId = orderId;
        this.showtimeId = showtimeId;
        this.seatIds = seatIds;
        this.amount = amount;
    }
}
