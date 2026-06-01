package com.dto.project.domain.product.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Getter
@Setter
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer price;

    @Column(name = "discount_rate", nullable = false)
    private Integer discountRate = 0;

    @Column(name = "delivery_fee")
    private Integer deliveryFee;

    @Column(name = "stock_quantity")
    private Integer stockQuantity;

    @Column(name = "short_description")
    private String shortDescription;

    @Lob
    private String description;

    private String brand;

    private String origin;

    @Lob
    @Column(name = "spec_info")
    private String specInfo;
    
    @Column(name = "alt_text")
    private String altText;

    @Column(name = "weight_score")
    private Integer weightScore;

    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 비즈니스 로직 (재고 차감)
    public void decreaseStock(int quantity) {
        if (this.stockQuantity < quantity) {
            throw new IllegalArgumentException("재고가 부족합니다. (현재 재고: " + this.stockQuantity + "개)");
        }
        this.stockQuantity -= quantity;
    }

    // 상태 변경
    public void changeStatus(String status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }


    public void updateProductInfo(
            Long categoryId, String name, Integer price, Integer deliveryFee,
            Integer stockQuantity, String shortDescription, String description,
            String brand, String origin, String specInfo, String altText) {
        this.categoryId = categoryId;
        this.name = name;
        this.price = price;
        this.deliveryFee = deliveryFee;
        this.stockQuantity = stockQuantity;
        this.shortDescription = shortDescription;
        this.description = description;
        this.brand = brand;
        this.origin = origin;
        this.specInfo = specInfo;
        this.altText = altText;
        this.updatedAt = LocalDateTime.now();

        if (this.status == null) {
            this.status = "ON_SALE";
        }

    }
}