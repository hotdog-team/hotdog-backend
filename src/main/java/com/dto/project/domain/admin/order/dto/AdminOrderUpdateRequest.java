package com.dto.project.domain.admin.order.dto;

import lombok.Getter;

@Getter
public class AdminOrderUpdateRequest {

    // 변경할 주문 상태
    private String status;

    // 등록할 운송장 번호
    private String trackingNumber;
}