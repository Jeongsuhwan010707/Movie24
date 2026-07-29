package project.movie24.payment.dto;

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
}
