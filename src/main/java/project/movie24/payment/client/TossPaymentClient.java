package project.movie24.payment.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Slf4j
@Component
public class TossPaymentClient {

    private final RestClient restClient;

    public TossPaymentClient(@Value("${toss.secret-key}") String secretKey) {
        String encodedAuth = Base64.getEncoder().encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
        this.restClient = RestClient.builder()
                .baseUrl("https://api.tosspayments.com")
                .defaultHeader("Authorization", "Basic " + encodedAuth)
                .build();
    }

    /**
     * 결제 승인. 2xx 응답이 아니면 RestClientResponseException이 발생한다.
     */
    public void confirm(String paymentKey, String orderId, int amount) {
        try {
            restClient.post()
                    .uri("/v1/payments/confirm")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("paymentKey", paymentKey, "orderId", orderId, "amount", amount))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException e) {
            log.warn("토스페이먼츠 결제 승인 실패: {}", e.getResponseBodyAsString());
            throw new IllegalStateException("결제 승인에 실패했습니다.", e);
        }
    }

    /**
     * 결제 승인 이후 예약 확정에 실패한 경우(좌석 중복 등) 결제를 취소해 과금을 되돌린다.
     */
    public void cancel(String paymentKey, String reason) {
        try {
            restClient.post()
                    .uri("/v1/payments/{paymentKey}/cancel", paymentKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("cancelReason", reason))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException e) {
            log.error("토스페이먼츠 결제 취소 실패 paymentKey={}, response={}", paymentKey, e.getResponseBodyAsString(), e);
        }
    }
}
