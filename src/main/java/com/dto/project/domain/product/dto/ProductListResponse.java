package com.dto.project.domain.product.dto;

import com.dto.project.domain.product.entity.Product;
import com.dto.project.domain.product.entity.ProductImage;
import lombok.Getter;

@Getter
public class ProductListResponse {

    private Long id;
    private Long categoryId;
    private String name;
    private Integer price;
    private Integer deliveryFee;
    private String shortDescription;
    private String brand;
    private String altText;
    private String status;
    private String imageUrl;
    private Double averageRate;
    private Integer reviewCount;

    public ProductListResponse(Product product) {
        this.id = product.getId();
        this.categoryId = product.getCategoryId();
        this.name = product.getName();
        this.price = product.getPrice();
        this.deliveryFee = product.getDeliveryFee();
        this.shortDescription = product.getShortDescription();
        this.brand = product.getBrand();
        this.altText = product.getAltText();
        this.status = product.getStatus();

        this.averageRate = product.getAverageRate();
        this.reviewCount = product.getReviewCount();
        
        this.imageUrl = product.getImages() == null
                ? ""
                : product.getImages().stream()
                    .filter(ProductImage::isMain)
                    .map(ProductImage::getImageUrl)
                    .filter(url -> url != null && !url.isBlank())
                    .findFirst()
                    .orElseGet(() ->
                        product.getImages().stream()
                            .map(ProductImage::getImageUrl)
                            .filter(url -> url != null && !url.isBlank())
                            .findFirst()
                            .orElse("")
                    );
    }
}