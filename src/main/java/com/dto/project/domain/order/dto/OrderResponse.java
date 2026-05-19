package com.dto.project.domain.order.dto;

import com.dto.project.domain.order.entity.Order;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class OrderResponse {
    private Long orderId;
    private String orderStatus;     // ORDERED, CANCELED, SHIPPED
    private int totalPrice;         // 총 결제 금액
    private LocalDateTime orderDate; // 주문 일시

    public OrderResponse(Order order) {
        this.orderId = order.getId();
        this.orderStatus = order.getStatus().name();
        this.totalPrice = order.getTotalAmount();
        this.orderDate = order.getCreatedAt();
    }
}