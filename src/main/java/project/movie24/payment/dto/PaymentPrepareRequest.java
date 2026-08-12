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
}
