package com.dto.project.domain.cart.entity;

import com.dto.project.domain.order.entity.ProductSource;
import com.dto.project.domain.product.entity.Product;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "cart_items")
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // TODO: JWT 연결 후 실제 로그인 회원 id로 교체
    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductSource source = ProductSource.INTERNAL;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "naver_product_id")
    private String naverProductId;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "image_url")
    private String imageUrl;

    private Integer price;
    
    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Cart(Long memberId, ProductSource source, Product product, String naverProductId, 
    			String productName, String imageUrl, Integer price, Integer quantity) {
        this.memberId = memberId;
        this.source = source;
        this.product = product;
        this.naverProductId = naverProductId;
        this.productName = productName;
        this.imageUrl = imageUrl;
        this.price = price;
        this.quantity = quantity;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void increaseQuantity(Integer quantity) {
        this.quantity += quantity;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateQuantity(Integer quantity) {
        this.quantity = quantity;
        this.updatedAt = LocalDateTime.now();
    }
}