package project.movie24.store.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import project.movie24.store.domain.PendingStoreOrder;
import project.movie24.store.domain.StoreCategory;
import project.movie24.store.domain.StoreOrder;
import project.movie24.store.dto.CartItemResponse;
import project.movie24.store.dto.StoreItemResponse;
import project.movie24.store.service.CartService;
import project.movie24.store.service.StoreCheckoutService;
import project.movie24.store.service.StoreItemService;
import project.movie24.user.domain.UserPrincipal;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class StoreController {

    private final StoreItemService storeItemService;
    private final CartService cartService;
    private final StoreCheckoutService storeCheckoutService;

    @Value("${toss.client-key}")
    private String tossClientKey;

    @GetMapping("/store")
    public String index(@AuthenticationPrincipal UserPrincipal principal, Model model) {
        model.addAttribute("packages", storeItemService.findByCategory(StoreCategory.PACKAGE).stream().map(StoreItemResponse::from).toList());
        model.addAttribute("tickets", storeItemService.findByCategory(StoreCategory.TICKET).stream().map(StoreItemResponse::from).toList());
        model.addAttribute("giftCards", storeItemService.findByCategory(StoreCategory.GIFT_CARD).stream().map(StoreItemResponse::from).toList());
        model.addAttribute("cartCount", principal != null ? cartService.countItems(principal.getUser().getId()) : 0);
        return "store/index";
    }

    @GetMapping("/store/bag")
    public String bag(@AuthenticationPrincipal UserPrincipal principal, Model model) {
        List<CartItemResponse> cartItems = cartService.findCart(principal.getUser().getId());
        int totalAmount = cartItems.stream().mapToInt(CartItemResponse::getLineTotal).sum();

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalAmount", totalAmount);
        return "store/bag";
    }

    @GetMapping("/store/bagPay")
    public String bagPay(@AuthenticationPrincipal UserPrincipal principal,
                          @RequestParam List<Long> cartItemIds,
                          Model model) {
        List<CartItemResponse> cartItems = cartService.findOwnedByIds(principal.getUser().getId(), cartItemIds).stream()
                .map(CartItemResponse::from)
                .toList();
        int totalAmount = cartItems.stream().mapToInt(CartItemResponse::getLineTotal).sum();
        String cartItemIdsCsv = cartItemIds.stream().map(String::valueOf).collect(Collectors.joining(","));

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalAmount", totalAmount);
        model.addAttribute("cartItemIdsCsv", cartItemIdsCsv);
        model.addAttribute("user", principal.getUser());
        model.addAttribute("tossClientKey", tossClientKey);
        return "store/bagPay";
    }

    @GetMapping("/store/payRedirect")
    public String payRedirect(@RequestParam String paymentKey,
                               @RequestParam String orderId,
                               @AuthenticationPrincipal UserPrincipal principal,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        PendingStoreOrder pending = storeCheckoutService.peekPending(session, orderId);
        try {
            if (principal == null) {
                throw new IllegalStateException("로그인이 만료되었습니다. 다시 로그인 후 시도해주세요.");
            }
            StoreOrder order = storeCheckoutService.confirmAndCreateOrder(session, principal.getUser().getId(), paymentKey, orderId);
            return "redirect:/store/payDone?orderId=" + order.getId();
        } catch (RuntimeException e) {
            redirectAttributes.addAttribute("payError", e.getMessage());
            if (pending == null) {
                return "redirect:/store/bag";
            }
            redirectAttributes.addAttribute("cartItemIds", pending.getCartItemIds());
            return "redirect:/store/bagPay";
        }
    }

    @GetMapping("/store/payDone")
    public String payDone(@RequestParam(required = false) Long orderId,
                           @AuthenticationPrincipal UserPrincipal principal,
                           Model model) {
        if (orderId != null && principal != null) {
            try {
                StoreOrder order = storeCheckoutService.findOwned(orderId, principal.getUser().getId());
                model.addAttribute("order", order);
                model.addAttribute("orderItems", storeCheckoutService.findOrderItems(orderId));
            } catch (RuntimeException ignored) {
                // 본인 주문이 아니거나 존재하지 않으면 일반 완료 화면만 보여준다.
            }
        }
        return "store/payDone";
    }
}
