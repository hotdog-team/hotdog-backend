package com.dto.project.domain.product.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class ProductPageResponse {

    private final List<ProductListResponse> content;
    private final int totalElements;
    private final int totalPages;
    private final int number;
    private final int size;

    public ProductPageResponse(List<ProductListResponse> content, int totalElements, int number, int size) {
        this.content = content;
        this.totalElements = totalElements;
        this.totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 1;
        this.number = number;
        this.size = size;
    }
}
