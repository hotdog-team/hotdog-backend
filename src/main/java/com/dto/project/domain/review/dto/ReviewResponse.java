package com.dto.project.domain.review.dto;

import com.dto.project.domain.product.entity.Product;
import com.dto.project.domain.product.entity.ProductImage;
import com.dto.project.domain.review.entity.Review;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReviewResponse {

    private Long reviewId;
    private Long productId;
    private String productName;
    private String productImageUrl;
    private Long memberId;
    private String memberName;
    private Integer rating;
    private String content;
    private String imageUrl;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ReviewResponse from(Review review) {
        return ReviewResponse.builder()
                .reviewId(review.getId())
                .productId(review.getProduct().getId())
                .productName(review.getProduct().getName())
                .productImageUrl(resolveProductImageUrl(review))
                .memberId(review.getMember().getId())
                .memberName(review.getMember().getName())
                .rating(review.getRating())
                .content(review.getContent())
                .imageUrl(review.getImageUrl())
                .status(review.getStatus())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }

    private static String resolveProductImageUrl(Review review) {
        if (review.getOrderItem() != null && review.getOrderItem().getImageUrl() != null) {
            return review.getOrderItem().getImageUrl();
        }

        Product product = review.getProduct();
        if (product == null || product.getImages() == null || product.getImages().isEmpty()) {
            return null;
        }

        return product.getImages().stream()
                .filter(ProductImage::isMain)
                .map(ProductImage::getImageUrl)
                .findFirst()
                .orElse(product.getImages().get(0).getImageUrl());
    }
}