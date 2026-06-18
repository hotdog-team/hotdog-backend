package com.dto.project.external.toss.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TossConfirmRequest {
    private String paymentKey;
    private String orderId;
    private Integer amount;
}