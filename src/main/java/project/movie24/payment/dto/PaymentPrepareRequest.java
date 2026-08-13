package project.movie24.payment.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.List;

@Getter
public class PaymentPrepareRequest {

    @NotNull
    private Long showtimeId;

    @NotEmpty
    private List<Long> seatIds;

    @Min(0)
    private int usePoint = 0;

    private boolean useGradeDiscount = false;

    // 선택한 보유 쿠폰(UserCoupon) id. null/미지정이면 쿠폰 미사용.
    private Long userCouponId;

    // 선택한 보유 관람권/기프티콘(TicketVoucher) id. null/미지정이면 미사용.
    private Long ticketVoucherId;
}
