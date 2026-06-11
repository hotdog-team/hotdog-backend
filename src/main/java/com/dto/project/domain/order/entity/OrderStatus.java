package com.dto.project.domain.order.entity;

public enum OrderStatus {

    PENDING,              // 결제대기
    PROCESSING,           // 결제중
    COMPLETED,            // 결제완료
    BEFORE_SHIPMENT,      // 배송준비
    IN_TRANSIT,           // 배송중
    DELIVERED,            // 배송완료
    PARTIAL_CANCELLED,    // 부분취소
    CANCELLED,            // 취소완료
    RETURN_REQUESTED,     // 반품신청
    RETURN_COMPLETED      // 반품완료
}