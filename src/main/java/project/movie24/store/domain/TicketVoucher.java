package project.movie24.store.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.movie24.user.domain.User;

import java.time.LocalDateTime;

/**
 * 스토어에서 구매한 TICKET/GIFT_CARD 상품 1건(수량 1개 단위)을 예매 결제에서 실제로 쓸 수 있는
 * "보유 중" 인스턴스로 추적한다. 구매 시점의 상품명/금액을 스냅샷으로 남겨(StoreOrderItem과 동일한 이유),
 * 이후 StoreItem이 바뀌어도 이미 발급된 관람권/기프티콘의 가치는 그대로 유지된다.
 */
@Entity
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class TicketVoucher {

    @Id @GeneratedValue
    @Column(name = "ticket_voucher_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // 어떤 주문에서 발급됐는지 추적용. StoreOrderItem과 연관관계를 맺지 않고 id만 저장한다.
    private Long storeOrderItemId;

    @Enumerated(EnumType.STRING)
    private StoreCategory category;

    private String itemName;
    private Integer faceValue;

    @Enumerated(EnumType.STRING)
    private TicketVoucherStatus status;

    private LocalDateTime issuedAt;
    private LocalDateTime usedAt;

    // Reservation 엔티티와 직접 연관관계를 맺지 않고 id만 저장해, store 패키지가 reservation에 의존하지 않게 한다.
    private Long reservationId;

    public void markUsed(LocalDateTime usedAt, Long reservationId) {
        this.status = TicketVoucherStatus.USED;
        this.usedAt = usedAt;
        this.reservationId = reservationId;
    }

    public void markUnused() {
        this.status = TicketVoucherStatus.UNUSED;
        this.usedAt = null;
        this.reservationId = null;
    }
}
