package project.movie24.store.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.movie24.store.dto.CartItemAddRequest;
import project.movie24.store.dto.CartItemResponse;
import project.movie24.store.dto.CartQuantityRequest;
import project.movie24.store.dto.CartRemoveRequest;
import project.movie24.store.service.CartService;
import project.movie24.user.domain.UserPrincipal;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/store/cart")
@RequiredArgsConstructor
public class CartApiController {

    private final CartService cartService;

    @PostMapping
    public ResponseEntity<Void> add(@AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody CartItemAddRequest request) {
        cartService.addItem(principal.getUser().getId(), request.getStoreItemId(), request.getQuantity());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public ResponseEntity<List<CartItemResponse>> list(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(cartService.findCart(principal.getUser().getId()));
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> count(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(Map.of("count", cartService.countItems(principal.getUser().getId())));
    }

    @PutMapping("/{cartItemId}")
    public ResponseEntity<Void> updateQuantity(@AuthenticationPrincipal UserPrincipal principal,
                                                @PathVariable Long cartItemId,
                                                @Valid @RequestBody CartQuantityRequest request) {
        cartService.updateQuantity(principal.getUser().getId(), cartItemId, request.getQuantity());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<Void> remove(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long cartItemId) {
        cartService.removeItems(principal.getUser().getId(), List.of(cartItemId));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/remove")
    public ResponseEntity<Void> removeSelected(@AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody CartRemoveRequest request) {
        cartService.removeItems(principal.getUser().getId(), request.getCartItemIds());
        return ResponseEntity.noContent().build();
    }
}
