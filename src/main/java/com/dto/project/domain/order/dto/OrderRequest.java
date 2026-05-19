package com.dto.project.domain.order.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderRequest {

    private List<OrderItemDto> orderItems;
    private String deliveryAddress; // 배송지 주소
    private String requestMessage;  // 배송 요청사항

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class OrderItemDto {
        private Long productId;
        private int quantity;
    }
}