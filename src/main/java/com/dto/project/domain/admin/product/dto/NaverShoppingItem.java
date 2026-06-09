package com.dto.project.domain.admin.product.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NaverShoppingItem {
    private String title;
    private String link;
    private String image;
    private Integer lprice;
    private Integer hprice;
    private String mallName;
    private String productId;
    private String productType;
    private String maker;
    private String brand;
    private String category1;
    private String category2;
    private String category3;
    private String category4;
}