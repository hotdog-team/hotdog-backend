package com.dto.project.domain.order.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class OrderItemCancelRequest {

    // 취소할 주문 상품 ID 목록
    private List<Long> orderItemIds;
}