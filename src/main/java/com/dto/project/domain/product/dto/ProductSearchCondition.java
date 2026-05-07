package com.dto.project.domain.product.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductSearchCondition {

    private Long categoryId;
    private String keyword;
    private String sort;
}