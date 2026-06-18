package com.dto.project.domain.admin.review.service;

import com.dto.project.domain.product.entity.Product;
import com.dto.project.domain.product.repository.ProductRepository;
import com.dto.project.domain.review.dto.ReviewResponse;
import com.dto.project.domain.review.entity.Review;
import com.dto.project.domain.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;

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
        if (!StringUtils.hasText(status)) {
            throw new IllegalArgumentException("변경할 리뷰 상태가 필요합니다.");
        }

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다. ID: " + reviewId));

        review.changeStatus(status);

        if (review.getProduct() != null) {
            recalculateProductReviewStats(review.getProduct());
        }
    }

    /**
     * 3. 관리자 전용: 리뷰 삭제
     */
    public void deleteReviewByAdmin(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다. ID: " + reviewId));

        review.changeStatus("DELETED");

        if (review.getProduct() != null) {
            recalculateProductReviewStats(review.getProduct());
        }
    }

    /**
     * 4. 숨김 처리 후 Review 점수 등을 재계산합니다.
     */
    private void recalculateProductReviewStats(Product product) {
        long activeReviewCount =
                reviewRepository.countByProduct_IdAndStatus(product.getId(), "ACTIVE");

        Double averageRate = activeReviewCount > 0
                ? reviewRepository.findAverageRateByProductId(product.getId())
                : 0.0;

        product.updateReviewStats(
                averageRate,
                Math.toIntExact(activeReviewCount)
        );
        productRepository.save(product);
    }
}
