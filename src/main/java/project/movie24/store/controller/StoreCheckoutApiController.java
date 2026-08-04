package project.movie24.store.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.movie24.store.dto.CheckoutPrepareRequest;
import project.movie24.store.dto.CheckoutPrepareResponse;
import project.movie24.store.service.StoreCheckoutService;
import project.movie24.user.domain.UserPrincipal;

@RestController
@RequestMapping("/api/store/checkout")
@RequiredArgsConstructor
public class StoreCheckoutApiController {

    private final StoreCheckoutService storeCheckoutService;

    @PostMapping("/prepare")
    public CheckoutPrepareResponse prepare(@AuthenticationPrincipal UserPrincipal principal,
                                            @Valid @RequestBody CheckoutPrepareRequest request,
                                            HttpSession session) {
        return storeCheckoutService.prepare(session, principal.getUser().getId(), request);
    }
}
