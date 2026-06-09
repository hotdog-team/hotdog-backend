package com.dto.project.domain.admin.product.dto;

import lombok.Getter;

@Getter
public class NaverProductCreateRequest {

    private Long categoryId;

    private String title;
    private String image;
    private Integer lprice;
    private String brand;
    private String mallName;
    private String link;
    private String productId;
}