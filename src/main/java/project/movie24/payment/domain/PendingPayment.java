package project.movie24.payment.domain;

import lombok.Getter;

import java.io.Serializable;
import java.util.List;

@Getter
public class PendingPayment implements Serializable {

    private final String orderId;
    private final Long showtimeId;
    private final List<Long> seatIds;
    // 관람권/기프티콘·등급 할인·쿠폰 할인과 포인트 사용분을 뺀, 실제로 Toss에 결제 승인을 요청할 캐시 결제금액.
    private final int amount;
    private final int totalPrice;
    private final int usedPoint;
    private final int gradeDiscountAmount;
    private final Long userCouponId;
    private final int couponDiscountAmount;
    private final Long ticketVoucherId;
    private final int voucherDiscountAmount;

    public PendingPayment(String orderId, Long showtimeId, List<Long> seatIds, int amount, int totalPrice,
                           int usedPoint, int gradeDiscountAmount, Long userCouponId, int couponDiscountAmount,
                           Long ticketVoucherId, int voucherDiscountAmount) {
        this.orderId = orderId;
        this.showtimeId = showtimeId;
        this.seatIds = seatIds;
        this.amount = amount;
        this.totalPrice = totalPrice;
        this.usedPoint = usedPoint;
        this.gradeDiscountAmount = gradeDiscountAmount;
        this.userCouponId = userCouponId;
        this.couponDiscountAmount = couponDiscountAmount;
        this.ticketVoucherId = ticketVoucherId;
        this.voucherDiscountAmount = voucherDiscountAmount;
    }
}
