package com.dto.project.domain.cart.dto;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CartBulkDeleteRequest {

    private List<Long> cartIds;
}