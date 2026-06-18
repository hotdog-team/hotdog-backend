package com.dto.project.domain.cart.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartAddRequest {

    private Long productId;
    private Integer quantity;
}