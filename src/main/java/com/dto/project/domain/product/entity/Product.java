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
}