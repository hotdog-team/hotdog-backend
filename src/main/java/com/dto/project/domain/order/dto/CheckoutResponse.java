package com.dto.project.domain.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class CheckoutResponse {

    private List<Item> items;
    private Integer totalAmount;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class Item {
        private Long cartId;
        private Long productId;
        private String productName;
        private String imageUrl;
        private Integer quantity;
        private Integer unitPrice;
        private Integer totalPrice;
        private Integer discountRate;
    }
}