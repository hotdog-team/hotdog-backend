package com.dto.project.domain.order.entity;

import java.time.LocalDateTime;

import com.dto.project.domain.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "order_items")
public class OrderItem {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // N:1 관계 - 어떤 주문에 속해 있는지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // 상품 출처 구분 (필수)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductSource source;

    // 1. 사내 상품일 경우 (NAVER 상품일 땐 null 허용)
    // N:1 관계 - 어떤 상품을 샀는지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = true)
    private Product product;

    // 2. 네이버 상품일 경우 (스냅샷 저장 영역)
    @Column(name = "naver_product_id")
    private String naverProductId;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "category")
    private String category;

    @Column(name = "description")
    private String description;


    @Column(nullable = false)
    private int quantity; // 주문 수량

    @Column(name = "price_at_order", nullable = false)
    private int priceAtOrder;

    // 주문 상품 상태
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderItemStatus status = OrderItemStatus.ORDERED;

    // 주문 상품 취소 일시
    private LocalDateTime cancelledAt;
    
    // Order 엔티티의 연관관계 편의 메서드(addOrderItem)에서 사용하기 위한 Setter
    public void setOrder(Order order) {
        this.order = order;
    }
    
    // 주문 상품 취소
    public void cancel() {

        // 이미 취소된 상품인지 검증
        if (this.status == OrderItemStatus.CANCELLED) {
            throw new IllegalStateException("이미 취소된 주문 상품입니다.");
        }

        this.status = OrderItemStatus.CANCELLED;

        this.cancelledAt = LocalDateTime.now();
    }
}