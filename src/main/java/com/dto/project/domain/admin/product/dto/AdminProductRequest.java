package com.dto.project.domain.admin.product.dto;

import lombok.Getter;
import java.util.List;

@Getter
public class AdminProductRequest {
    private Long categoryId;
    private String name;
    private Integer price;
    private Integer deliveryFee;
    private Integer stockQuantity;
    private String shortDescription;
    private String description;
    private String brand;
    private String origin;
    private String specInfo;
    private String altText;

    // 다중 메타태그 및 이미지 처리
    private List<Long> metaTagIds;
    private List<String> images;
}