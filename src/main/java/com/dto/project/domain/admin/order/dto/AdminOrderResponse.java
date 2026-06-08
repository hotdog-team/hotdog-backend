package com.dto.project.domain.admin.order.dto;

import com.dto.project.domain.order.entity.Order;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminOrderResponse {

    // 주문 ID
    private Long orderId;

    // 회원 ID
    private Long memberId;

    // 수령인 이름
    private String receiverName;

    // 수령인 전화번호
    private String receiverPhone;

    // 배송 주소
    private String deliveryAddress;

    // 결제 수단
    private String paymentMethod;

    // 주문 상태
    private String status;

    // 운송장 번호
    private String trackingNumber;

    // 주문 생성일
    private LocalDateTime createdAt;

    // Order 엔티티를 관리자 주문 응답 DTO로 변환
    public static AdminOrderResponse from(Order order) {
        return AdminOrderResponse.builder()
                .orderId(order.getId())
                .memberId(order.getMember().getId())
                .receiverName(order.getReceiverName())
                .receiverPhone(order.getReceiverPhone())
                .deliveryAddress(order.getDeliveryAddress())
                .paymentMethod(order.getPaymentMethod() != null ? order.getPaymentMethod().name() : null)
                .status(order.getStatus().name())
                .trackingNumber(order.getTrackingNumber())
                .createdAt(order.getCreatedAt())
                .build();
    }
}