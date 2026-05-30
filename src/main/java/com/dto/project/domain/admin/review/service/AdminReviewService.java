package com.dto.project.domain.admin.review.service;

import com.dto.project.domain.review.dto.ReviewResponse;
import com.dto.project.domain.review.entity.Review;
import com.dto.project.domain.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminReviewService {

    private final ReviewRepository reviewRepository;

    /**
     * 1. 관리자 전용: 전체 리뷰 열람
     */
    @Transactional(readOnly = true)
    public List<ReviewResponse> getAllReviewsForAdmin() {
        return reviewRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(ReviewResponse::from)
                .toList();
    }

    /**
     * 2. 관리자 전용: 악성 리뷰 숨김 처리
     */
    public void changeReviewStatus(Long reviewId, String status) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다. ID: " + reviewId));

        // "HIDDEN" 또는 "ACTIVE" 등 전달받은 상태로 변경
        review.changeStatus(status);
    }

    /**
     * 3. 관리자 전용: 리뷰 삭제
     */
    public void deleteReviewByAdmin(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다. ID: " + reviewId));

        review.changeStatus("DELETED");
    }
}
