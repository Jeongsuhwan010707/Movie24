package project.movie24.store.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.movie24.common.EntityFinders;
import project.movie24.store.domain.CartItem;
import project.movie24.store.domain.StoreItem;
import project.movie24.store.dto.CartItemResponse;
import project.movie24.store.repository.CartItemRepository;
import project.movie24.store.repository.StoreItemRepository;
import project.movie24.user.repository.UserRepository;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final StoreItemRepository storeItemRepository;
    private final UserRepository userRepository;

    public CartItem addItem(Long userId, Long storeItemId, int quantity) {
        StoreItem storeItem = EntityFinders.getOrThrow(storeItemRepository, storeItemId, "스토어 상품");

        return cartItemRepository.findByUserIdAndStoreItemId(userId, storeItemId)
                .map(existing -> {
                    existing.increaseQuantity(quantity);
                    return existing;
                })
                .orElseGet(() -> cartItemRepository.save(CartItem.builder()
                        .user(userRepository.getReferenceById(userId))
                        .storeItem(storeItem)
                        .quantity(quantity)
                        .build()));
    }

    public void updateQuantity(Long userId, Long cartItemId, int quantity) {
        CartItem cartItem = getOwnedOrThrow(userId, cartItemId);
        cartItem.changeQuantity(quantity);
    }

    public void removeItems(Long userId, List<Long> cartItemIds) {
        List<CartItem> owned = cartItemRepository.findByIdInAndUserId(cartItemIds, userId);
        cartItemRepository.deleteAll(owned);
    }

    @Transactional(readOnly = true)
    public List<CartItemResponse> findCart(Long userId) {
        return cartItemRepository.findByUserIdWithStoreItem(userId).stream()
                .map(CartItemResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public long countItems(Long userId) {
        return cartItemRepository.countByUserId(userId);
    }

    /**
     * 결제 준비/승인 시 본인 소유 장바구니 항목만 대상으로 하도록, 요청된 id 중 실제로 본인 소유인 것만 조회한다.
     * 존재하지 않거나 다른 사용자의 항목이 섞여 있으면 개수가 달라지므로 즉시 걸러낸다.
     */
    @Transactional(readOnly = true)
    public List<CartItem> findOwnedByIds(Long userId, List<Long> cartItemIds) {
        List<CartItem> owned = cartItemRepository.findByIdInAndUserId(cartItemIds, userId);
        if (owned.size() != cartItemIds.size()) {
            throw new IllegalArgumentException("존재하지 않거나 본인 소유가 아닌 장바구니 항목이 포함되어 있습니다.");
        }
        return owned;
    }

    private CartItem getOwnedOrThrow(Long userId, Long cartItemId) {
        CartItem cartItem = EntityFinders.getOrThrow(cartItemRepository, cartItemId, "장바구니 항목");
        if (!cartItem.getUser().getId().equals(userId)) {
            throw new IllegalStateException("본인의 장바구니 항목만 변경할 수 있습니다.");
        }
        return cartItem;
    }
}
