package com.dto.project.external.toss.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TossPaymentResponse {
    private String paymentKey;
    private String orderId;
    private String status;
    private String method;
    private Integer totalAmount;
    private String approvedAt;
}