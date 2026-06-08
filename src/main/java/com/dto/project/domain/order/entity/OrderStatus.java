package com.dto.project.domain.order.entity;

public enum OrderStatus {
    PENDING,          // 결제대기
    PROCESSING,       // 결제중
    COMPLETED,        // 결제완료
    BEFORE_SHIPMENT,  // 배송전
    IN_TRANSIT,       // 배송중
    DELIVERED,        // 배송완료
    CANCELLED          // 취소완료
}