package com.dto.project.domain.order.dto;

import com.dto.project.domain.order.entity.ProductSource;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class CheckoutRequest {

    // 장바구니 주문서용
    private List<Long> cartIds;

    // 바로구매 주문서용
    private ProductSource source;
    private Long productId;
    private Integer quantity;
}