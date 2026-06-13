package com.dto.project.domain.order.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemReturnRequest {

    private List<Long> orderItemIds;

    private String reason;

    private String detailReason;
}