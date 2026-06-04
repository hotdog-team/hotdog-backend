package com.dto.project.domain.review.controller;

import com.dto.project.domain.review.dto.ReviewCreateRequest;
import com.dto.project.domain.review.dto.ReviewResponse;
import com.dto.project.domain.review.dto.ReviewUpdateRequest;
import com.dto.project.domain.review.service.ReviewService;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // 상품 리뷰 목록 조회
    @GetMapping("/products/{productId}/reviews")
    public Page<ReviewResponse> getProductReviews(
            @PathVariable Long productId,
            Pageable pageable
    ) {
        return reviewService.getProductReviews(productId, pageable);
    }

    // 리뷰 작성
    @PostMapping("/orders/items/{orderItemId}/reviews")
    public String createReview(
            @RequestParam Long memberId,
            @PathVariable Long orderItemId,
            @RequestBody ReviewCreateRequest request
    ) {
        reviewService.createReview(memberId, orderItemId, request);
        return "리뷰 작성 완료";
    }

    // 내 리뷰 조회
    @GetMapping("/reviews/my")
    public Page<ReviewResponse> getMyReviews(
            @RequestParam Long memberId,
            Pageable pageable
    ) {
        return reviewService.getMyReviews(memberId, pageable);
    }

    // 리뷰 수정
    @PatchMapping("/reviews/{reviewId}")
    public String updateReview(
            @RequestParam Long memberId,
            @PathVariable Long reviewId,
            @RequestBody ReviewUpdateRequest request
    ) {
        reviewService.updateReview(memberId, reviewId, request);
        return "리뷰 수정 완료";
    }

    // 리뷰 삭제
    @DeleteMapping("/reviews/{reviewId}")
    public String deleteReview(
            @RequestParam Long memberId,
            @PathVariable Long reviewId
    ) {
        reviewService.deleteReview(memberId, reviewId);
        return "리뷰 삭제 완료";
    }
}