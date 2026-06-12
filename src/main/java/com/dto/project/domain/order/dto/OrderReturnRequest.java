package com.dto.project.domain.order.dto;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
public class OrderReturnRequest {

    private String reason;
    private String detailReason;
}