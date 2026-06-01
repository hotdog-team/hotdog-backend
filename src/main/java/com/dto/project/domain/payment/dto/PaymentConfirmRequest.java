package com.dto.project.domain.payment.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PaymentConfirmRequest {
    private String paymentKey;
    private String tossOrderId;
    private Long orderId;           // 주문 ID
    private Integer amount;
    private String paymentMethod;   // CARD, CASH
}