package com.dto.project.domain.cart.dto;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
public class CartBulkAddRequest {

    private List<CartAddItem> items;

    @Getter
    @NoArgsConstructor
    public static class CartAddItem {
        private Long productId;
        private Integer quantity;
    }
}