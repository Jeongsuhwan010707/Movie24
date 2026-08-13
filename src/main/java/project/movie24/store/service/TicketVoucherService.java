package project.movie24.store.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.movie24.store.domain.StoreCategory;
import project.movie24.store.domain.StoreOrderItem;
import project.movie24.store.domain.TicketVoucher;
import project.movie24.store.domain.TicketVoucherStatus;
import project.movie24.store.repository.TicketVoucherRepository;
import project.movie24.user.domain.User;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class TicketVoucherService {

    private final TicketVoucherRepository ticketVoucherRepository;

    /**
     * 스토어 주문 확정 시 호출된다. TICKET/GIFT_CARD 카테고리 상품만 관람권/기프티콘으로 발급하고,
     * 수량만큼 낱개(1장 = 1 인스턴스)로 쪼개 발급해 예매 결제에서 한 장씩 골라 쓸 수 있게 한다.
     */
    public void issueFromOrderItem(User user, StoreOrderItem orderItem) {
        StoreCategory category = orderItem.getStoreItem().getCategory();
        if (category != StoreCategory.TICKET && category != StoreCategory.GIFT_CARD) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < orderItem.getQuantity(); i++) {
            ticketVoucherRepository.save(TicketVoucher.builder()
                    .user(user)
                    .storeOrderItemId(orderItem.getId())
                    .category(category)
                    .itemName(orderItem.getItemName())
                    .faceValue(orderItem.getUnitPrice())
                    .status(TicketVoucherStatus.UNUSED)
                    .issuedAt(now)
                    .build());
        }
    }

    @Transactional(readOnly = true)
    public List<TicketVoucher> findMyUsable(Long userId) {
        return ticketVoucherRepository.findByUser_IdAndStatusOrderByIssuedAtAsc(userId, TicketVoucherStatus.UNUSED);
    }

    @Transactional(readOnly = true)
    public TicketVoucher findOwned(Long ticketVoucherId, Long userId) {
        TicketVoucher voucher = ticketVoucherRepository.findByIdAndUser_Id(ticketVoucherId, userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 관람권/기프티콘입니다."));
        if (voucher.getStatus() != TicketVoucherStatus.UNUSED) {
            throw new IllegalStateException("이미 사용된 관람권/기프티콘입니다.");
        }
        return voucher;
    }

    /**
     * 액면가만큼 할인하되 결제 금액을 초과할 수 없다(금액권처럼 잔액 환불은 없음).
     */
    @Transactional(readOnly = true)
    public int previewDiscount(TicketVoucher voucher, int orderAmount) {
        return Math.max(0, Math.min(voucher.getFaceValue(), orderAmount));
    }

    public TicketVoucher markUsed(Long ticketVoucherId, Long userId, Long reservationId) {
        TicketVoucher voucher = findOwned(ticketVoucherId, userId);
        voucher.markUsed(LocalDateTime.now(), reservationId);
        return voucher;
    }

    /**
     * 예매 취소 시 관람권/기프티콘 사용을 되돌린다. 해당 예매에 쓰인 게 없으면 아무 일도 하지 않는다.
     */
    public void cancelUse(Long reservationId) {
        ticketVoucherRepository.findByReservationId(reservationId)
                .ifPresent(TicketVoucher::markUnused);
    }
}
