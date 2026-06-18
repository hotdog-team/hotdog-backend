package com.dto.project.domain.admin.review.controller;

import com.dto.project.domain.admin.review.dto.AdminReviewStatusRequest;
import com.dto.project.domain.review.dto.ReviewResponse;
import com.dto.project.domain.admin.review.service.AdminReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/reviews")
@RequiredArgsConstructor
public class AdminReviewController {

    private final AdminReviewService adminReviewService;

    // 전체 리뷰 리스트 열람
    @GetMapping
    public List<ReviewResponse> getAllReviews() {
        return adminReviewService.getAllReviewsForAdmin();
    }

    //부적절한 리뷰 숨김 처리
    @PatchMapping("/{reviewId}")
    public String hideReview(
            @PathVariable Long reviewId,
            @RequestBody AdminReviewStatusRequest request
    ) {
        adminReviewService.changeReviewStatus(reviewId, request.getStatus());
        return "리뷰 상태가 [" + request.getStatus() + "]로 변경되었습니다.";
    }

    //부적절한 리뷰 삭제 처리
    @DeleteMapping("/{reviewId}")
    public String deleteReviewByAdmin(@PathVariable Long reviewId) {
        adminReviewService.deleteReviewByAdmin(reviewId);
        return "관리자 권한으로 리뷰가 삭제 처리되었습니다.";
    }
}