package project.movie24.store.service;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.movie24.common.EntityFinders;
import project.movie24.payment.client.TossPaymentClient;
import project.movie24.store.domain.CartItem;
import project.movie24.store.domain.PendingStoreOrder;
import project.movie24.store.domain.StoreOrder;
import project.movie24.store.domain.StoreOrderItem;
import project.movie24.store.dto.CheckoutPrepareRequest;
import project.movie24.store.dto.CheckoutPrepareResponse;
import project.movie24.store.dto.StoreOrderItemResponse;
import project.movie24.store.dto.StoreOrderResponse;
import project.movie24.store.repository.CartItemRepository;
import project.movie24.store.repository.StoreOrderItemRepository;
import project.movie24.store.repository.StoreOrderRepository;
import project.movie24.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoreCheckoutService {

    private static final String SESSION_KEY = "PENDING_STORE_ORDER";

    private final CartService cartService;
    private final CartItemRepository cartItemRepository;
    private final StoreOrderRepository storeOrderRepository;
    private final StoreOrderItemRepository storeOrderItemRepository;
    private final UserRepository userRepository;
    private final TossPaymentClient tossPaymentClient;

    @Transactional(readOnly = true)
    public CheckoutPrepareResponse prepare(HttpSession session, Long userId, CheckoutPrepareRequest request) {
        List<CartItem> cartItems = cartService.findOwnedByIds(userId, request.getCartItemIds());

        int amount = cartItems.stream()
                .mapToInt(c -> c.getStoreItem().getPrice() * c.getQuantity())
                .sum();
        String orderId = "movie24-store-" + UUID.randomUUID();

        session.setAttribute(SESSION_KEY, new PendingStoreOrder(orderId, request.getCartItemIds(), amount));

        return CheckoutPrepareResponse.builder()
                .orderId(orderId)
                .orderName(buildOrderName(cartItems))
                .amount(amount)
                .build();
    }

    @Transactional(readOnly = true)
    public PendingStoreOrder peekPending(HttpSession session, String orderId) {
        PendingStoreOrder pending = (PendingStoreOrder) session.getAttribute(SESSION_KEY);
        return (pending != null && pending.getOrderId().equals(orderId)) ? pending : null;
    }

    @Transactional
    public StoreOrder confirmAndCreateOrder(HttpSession session, Long userId, String paymentKey, String orderId) {
        PendingStoreOrder pending = peekPending(session, orderId);
        if (pending == null) {
            throw new IllegalStateException("결제 정보를 찾을 수 없습니다. 처음부터 다시 시도해주세요.");
        }

        // 결제 승인 시 우리가 서버에서 계산해 둔 금액(pending.amount)을 보낸다.
        // 클라이언트가 위젯 호출 전에 금액을 조작했다면, 토스에 실제 승인된 금액과 달라 여기서 거절된다.
        tossPaymentClient.confirm(paymentKey, orderId, pending.getAmount());

        try {
            List<CartItem> cartItems = cartService.findOwnedByIds(userId, pending.getCartItemIds());

            StoreOrder order = storeOrderRepository.save(StoreOrder.builder()
                    .user(userRepository.getReferenceById(userId))
                    .orderUid(orderId)
                    .totalAmount(pending.getAmount())
                    .orderedAt(LocalDateTime.now())
                    .build());

            for (CartItem cartItem : cartItems) {
                storeOrderItemRepository.save(StoreOrderItem.builder()
                        .storeOrder(order)
                        .storeItem(cartItem.getStoreItem())
                        .itemName(cartItem.getStoreItem().getName())
                        .unitPrice(cartItem.getStoreItem().getPrice())
                        .quantity(cartItem.getQuantity())
                        .build());
            }

            cartItemRepository.deleteAll(cartItems);
            session.removeAttribute(SESSION_KEY);
            return order;
        } catch (RuntimeException e) {
            // 결제는 승인됐는데 주문 확정에 실패한 경우, 결제를 취소해 과금을 되돌린다.
            tossPaymentClient.cancel(paymentKey, "주문 확정 실패로 인한 자동 취소");
            session.removeAttribute(SESSION_KEY);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public StoreOrder findOwned(Long storeOrderId, Long userId) {
        StoreOrder order = EntityFinders.getOrThrow(storeOrderRepository, storeOrderId, "스토어 주문");
        if (!order.getUser().getId().equals(userId)) {
            throw new IllegalStateException("본인의 주문만 조회할 수 있습니다.");
        }
        return order;
    }

    @Transactional(readOnly = true)
    public List<StoreOrderItemResponse> findOrderItems(Long storeOrderId) {
        return storeOrderItemRepository.findByStoreOrderId(storeOrderId).stream()
                .map(StoreOrderItemResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StoreOrderResponse> findMyOrders(Long userId) {
        List<StoreOrder> orders = storeOrderRepository.findByUserIdOrderByOrderedAtDesc(userId);
        List<Long> orderIds = orders.stream().map(StoreOrder::getId).toList();

        Map<Long, List<StoreOrderItemResponse>> itemsByOrderId = storeOrderItemRepository.findByStoreOrderIdIn(orderIds).stream()
                .collect(Collectors.groupingBy(item -> item.getStoreOrder().getId(),
                        Collectors.mapping(StoreOrderItemResponse::from, Collectors.toList())));

        return orders.stream()
                .map(order -> StoreOrderResponse.from(order, itemsByOrderId.getOrDefault(order.getId(), List.of())))
                .toList();
    }

    private String buildOrderName(List<CartItem> cartItems) {
        String firstName = cartItems.get(0).getStoreItem().getName();
        return cartItems.size() > 1 ? firstName + " 외 " + (cartItems.size() - 1) + "건" : firstName;
    }
}
