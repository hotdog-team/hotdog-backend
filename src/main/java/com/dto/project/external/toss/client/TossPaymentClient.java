package com.dto.project.external.toss.client;

import com.dto.project.external.toss.dto.TossConfirmRequest;
import com.dto.project.external.toss.dto.TossPaymentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class TossPaymentClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${toss.secret-key}")
    private String tossSecretKey;

    public TossPaymentResponse confirmPayment(TossConfirmRequest request) {
        String url = "https://api.tosspayments.com/v1/payments/confirm";

        // 시크릿 키 인코딩
        String encodedAuthKey = Base64.getEncoder().encodeToString((tossSecretKey + ":").getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + encodedAuthKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> params = new HashMap<>();
        params.put("paymentKey", request.getPaymentKey());
        params.put("orderId", request.getOrderId());
        params.put("amount", request.getAmount());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(params, headers);

        try {
            return restTemplate.postForObject(url, entity, TossPaymentResponse.class);
        } catch (Exception e) {
            log.error("토스페이먼츠 최종 결제 승인 실패: {}", e.getMessage());
            throw new IllegalStateException("토스 결제 승인 중 오류가 발생했습니다.");
        }
    }
}