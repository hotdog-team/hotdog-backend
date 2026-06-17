package com.dto.project.domain.product.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProductSearchCondition {

    private Long categoryId;
    private String keyword;
    private String sort;
    private String match;
    private List<Long> metaTagIds;
    private Integer minPrice;
    private Integer maxPrice;
    private Integer size;
    private Integer page;
}