package com.dto.project.domain.review.dto;

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
    private Long memberId;
    private String memberName;
    private Integer rating;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ReviewResponse from(Review review) {
        return ReviewResponse.builder()
                .reviewId(review.getId())
                .productId(review.getProduct().getId())
                .productName(review.getProduct().getName())
                .memberId(review.getMember().getId())
                .memberName(review.getMember().getName())
                .rating(review.getRating())
                .content(review.getContent())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}