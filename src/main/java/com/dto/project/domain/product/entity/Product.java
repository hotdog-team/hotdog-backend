package com.dto.project.domain.product.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@EntityListeners(AuditingEntityListener.class)
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

    @Column(name = "sales_count")
    private Long salesCount = 0L;

    @Column(name = "weight_score")
    private Double weightScore;

    private String status;

    @Column(name = "created_at", updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "average_rate", nullable = false)
    private Double averageRate = 0.0;

    @Column(name = "review_count", nullable = false)
    private Integer reviewCount = 0;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "product_id")
    private List<ProductImage> images = new ArrayList<>();

    // WeightScore 적용
    public void adjustWeightScore(double delta) {
        double current = this.weightScore != null ? this.weightScore : 0;
        this.weightScore = Math.max(0, current + delta);
        this.updatedAt = LocalDateTime.now();
    }

    // 비즈니스 로직 (재고 차감)
    public void decreaseStock(int quantity) {
        if (this.stockQuantity < quantity) {
            throw new IllegalArgumentException("재고가 부족합니다. (현재 재고: " + this.stockQuantity + "개)");
        }
        this.stockQuantity -= quantity;
    }

    // 해당 상품 판매량 증가
    public void increaseSalesCount(int quantity) {
        this.salesCount += quantity;
    }

    // 상태 변경
    public void changeStatus(String status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }
    
    // 비즈니스 로직 (재고 복구)
    public void increaseStock(int quantity) {
        this.stockQuantity += quantity;
    }

    // 해당 상품 판매량 감수
    public void decreaseSalesCount(int quantity) {
        this.salesCount -= quantity;
    }

    public void updateProductInfo(
            Long categoryId, String name, Integer price, Integer deliveryFee,
            Integer stockQuantity, String shortDescription, String description,
            String brand, String origin, String specInfo, String altText) {
        this.categoryId = categoryId;
        this.name = name;
        this.price = price;
        this.discountRate = discountRate;
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

    public void updateReviewStats(Double averageRate, Integer reviewCount) {
        this.averageRate = averageRate;
        this.reviewCount = reviewCount;
    }
}