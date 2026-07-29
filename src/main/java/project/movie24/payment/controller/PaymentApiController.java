package project.movie24.payment.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.movie24.payment.dto.PaymentPrepareRequest;
import project.movie24.payment.dto.PaymentPrepareResponse;
import project.movie24.payment.service.PaymentService;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentApiController {

    private final PaymentService paymentService;

    @PostMapping("/prepare")
    public PaymentPrepareResponse prepare(@Valid @RequestBody PaymentPrepareRequest request, HttpSession session) {
        return paymentService.prepare(session, request);
    }
}
