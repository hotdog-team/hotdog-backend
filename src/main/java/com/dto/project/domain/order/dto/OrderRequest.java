package com.dto.project.domain.order.dto;

import com.dto.project.domain.order.entity.PaymentMethod;
import com.dto.project.domain.order.entity.ProductSource;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderRequest {

    private List<OrderItemDto> orderItems;
    private String receiverName;
    private String receiverPhone;
    private String deliveryAddress; // 배송지 주소
    private String requestMessage;  // 배송 요청사항

    // 결제 관련 정보
    private int totalAmount;
    private int deliveryFee;
    private PaymentMethod paymentMethod;

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class OrderItemDto {

        private ProductSource source; // 필수: INTERNAL or NAVER
        private int quantity;
        private int price; // 결제 시점의 단가 (사내/네이버 공통)

        // 1. 사내 상품 결제 시 (source == INTERNAL)
        private Long productId;

        // 2. 네이버 상품 결제 시 (source == NAVER)
        private String naverProductId;
        private String productName;
        private String imageUrl;
        private String category;
        private String description;
    }
}