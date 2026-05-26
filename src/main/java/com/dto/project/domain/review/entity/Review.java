package com.dto.project.domain.review.entity;

import com.dto.project.domain.member.entity.Member;
import com.dto.project.domain.order.entity.OrderItem;
import com.dto.project.domain.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 작성자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // 사내 상품일 경우만 연결
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    // 주문 상품 기준 리뷰 작성
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false, unique = true)
    private OrderItem orderItem;
    
    // 평점
    @Column(nullable = false)
    private Integer rating;

    // 리뷰 내용
    @Lob
    @Column(nullable = false, length = 1000)
    private String content;

    // 리뷰 이미지
    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "status")
    private String status = "ACTIVE";
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public Review(Member member, Product product, OrderItem orderItem, Integer rating, String content, String imageUrl) {
        this.member = member;
        this.product = product;
        this.orderItem = orderItem;
        this.rating = rating;
        this.content = content;
        this.imageUrl = imageUrl;
        this.status = "ACTIVE";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void update(Integer rating, String content, String imageUrl ) {
        this.rating = rating;
        this.content = content;
        this.imageUrl = imageUrl;
        this.updatedAt = LocalDateTime.now();
    }
}