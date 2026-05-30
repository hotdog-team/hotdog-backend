package com.dto.project.domain.review.controller;

import com.dto.project.domain.review.dto.ReviewCreateRequest;
import com.dto.project.domain.review.dto.ReviewResponse;
import com.dto.project.domain.review.dto.ReviewUpdateRequest;
import com.dto.project.domain.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // 상품 리뷰 목록 조회
    @GetMapping("/products/{productId}/reviews")
    public List<ReviewResponse> getProductReviews(@PathVariable Long productId) {
        return reviewService.getProductReviews(productId);
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
    public List<ReviewResponse> getMyReviews(@RequestParam Long memberId) {
        return reviewService.getMyReviews(memberId);
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