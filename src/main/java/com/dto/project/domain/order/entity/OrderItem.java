package com.dto.project.domain.order.entity;

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

    // N:1 관계 - 어떤 상품을 샀는지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private int quantity; // 주문 수량

    @Column(name = "price_at_order", nullable = false)
    private int priceAtOrder;

    // Order 엔티티의 연관관계 편의 메서드(addOrderItem)에서 사용하기 위한 Setter
    public void setOrder(Order order) {
        this.order = order;
    }
}