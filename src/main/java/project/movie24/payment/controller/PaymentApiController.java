package project.movie24.payment.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.movie24.payment.dto.PaymentPrepareRequest;
import project.movie24.payment.dto.PaymentPrepareResponse;
import project.movie24.payment.service.PaymentService;
import project.movie24.user.domain.UserPrincipal;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentApiController {

    private final PaymentService paymentService;

    @PostMapping("/prepare")
    public PaymentPrepareResponse prepare(@AuthenticationPrincipal UserPrincipal principal,
                                           @Valid @RequestBody PaymentPrepareRequest request, HttpSession session) {
        return paymentService.prepare(session, principal.getUser().getId(), request);
    }
}
