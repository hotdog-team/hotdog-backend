package com.dto.project.domain.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CartResponse {

    private Long cartId;
    private Long productId;
    private String productName;
    private Integer price;
    private Integer quantity;
    private String thumbnailImage;
    private Integer discountRate;
    private Integer salePrice;
    private Integer deliveryFee;
}