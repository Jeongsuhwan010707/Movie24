package project.movie24.payment.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentPrepareResponse {

    private String orderId;
    private String orderName;
    private int amount;
    private int totalPrice;
    private int usedPoint;
    private int gradeDiscountAmount;
    private int couponDiscountAmount;
}
