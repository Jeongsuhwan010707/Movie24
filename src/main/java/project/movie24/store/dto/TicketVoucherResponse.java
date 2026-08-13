package project.movie24.store.dto;

import lombok.Builder;
import lombok.Getter;
import project.movie24.store.domain.StoreCategory;
import project.movie24.store.domain.TicketVoucher;

@Getter
@Builder
public class TicketVoucherResponse {

    private Long id;
    private StoreCategory category;
    private String itemName;
    private Integer faceValue;

    public static TicketVoucherResponse from(TicketVoucher voucher) {
        return TicketVoucherResponse.builder()
                .id(voucher.getId())
                .category(voucher.getCategory())
                .itemName(voucher.getItemName())
                .faceValue(voucher.getFaceValue())
                .build();
    }
}
