package com.dto.project.domain.product.dto;

import com.dto.project.domain.product.entity.Product;
import lombok.Getter;

@Getter
public class ProductResponse {

    private Long id;
    private Long categoryId;
    private String name;

    private Integer originPrice;
    private Integer discountRate;
    private Integer salePrice;

    private Integer deliveryFee;
    private Integer stockQuantity;
    private String shortDescription;
    private String description;
    private String brand;
    private String origin;
    private String specInfo;
    private String altText;
    private Integer weightScore;
    private String status;

    public ProductResponse(Product product) {
        this.id = product.getId();
        this.categoryId = product.getCategoryId();
        this.name = product.getName();

        this.originPrice = product.getPrice();
        this.discountRate = product.getDiscountRate();
        this.salePrice = (int) (product.getPrice() * (1 - (this.discountRate / 100.0)));

        this.deliveryFee = product.getDeliveryFee();
        this.stockQuantity = product.getStockQuantity();
        this.shortDescription = product.getShortDescription();
        this.description = product.getDescription();
        this.brand = product.getBrand();
        this.origin = product.getOrigin();
        this.specInfo = product.getSpecInfo();
        this.altText = product.getAltText();
        this.weightScore = product.getWeightScore();
        this.status = product.getStatus();
    }
}