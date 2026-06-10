package com.dto.project.domain.review.service;

import com.dto.project.domain.member.entity.Member;
import com.dto.project.domain.member.repository.MemberRepository;
import com.dto.project.domain.order.entity.OrderItem;
import com.dto.project.domain.order.repository.OrderItemRepository;
import com.dto.project.domain.product.entity.Product;
import com.dto.project.domain.product.repository.ProductRepository;
import com.dto.project.domain.review.dto.ReviewCreateRequest;
import com.dto.project.domain.review.dto.ReviewResponse;
import com.dto.project.domain.review.dto.ReviewUpdateRequest;
import com.dto.project.domain.review.entity.Review;
import com.dto.project.domain.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final MemberRepository memberRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;

    public void createReview(Long memberId, Long orderItemId, ReviewCreateRequest request) {
        validateReviewRequest(request.getRating(), request.getContent());

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new IllegalArgumentException("주문 상품을 찾을 수 없습니다."));

        if (reviewRepository.existsByOrderItem(orderItem)) {
            throw new IllegalArgumentException("이미 해당 주문 상품에 리뷰를 작성했습니다.");
        }

        Review review = Review.builder()
                .member(member)
                .product(orderItem.getProduct())
                .orderItem(orderItem)
                .rating(request.getRating())
                .content(request.getContent())
                .imageUrl(request.getImageUrl())
                .build();

        reviewRepository.save(review);

        if (orderItem.getProduct() != null) {
            updateProductReviewStats(orderItem.getProduct());
        }
    }
    
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getProductReviews(Long productId, Pageable pageable) {
        return reviewRepository
                .findAllByProductIdAndStatusOrderByCreatedAtDesc(productId, "ACTIVE", pageable)
                .map(ReviewResponse::from);
    }
    

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getMyReviews(Long memberId, Pageable pageable) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        return reviewRepository.findAllByMemberAndStatusOrderByCreatedAtDesc(member, "ACTIVE", pageable)
                .map(ReviewResponse::from);
    }

    public void updateReview(Long memberId, Long reviewId, ReviewUpdateRequest request) {
        validateReviewRequest(request.getRating(), request.getContent());

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));

        if (!review.getMember().getId().equals(memberId)) {
            throw new IllegalArgumentException("본인이 작성한 리뷰만 수정할 수 있습니다.");
        }

        review.update(
                request.getRating(),
                request.getContent(),
                request.getImageUrl()
                   
        );
        updateProductReviewStats(review.getProduct());
    }

    public void deleteReview(Long memberId, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));

        if (!review.getMember().getId().equals(memberId)) {
            throw new IllegalArgumentException("본인이 작성한 리뷰만 삭제할 수 있습니다.");
        }

        review.changeStatus("DELETED");
        
        if (review.getProduct() != null) {
            updateProductReviewStats(review.getProduct());
        }
    }

    private void validateReviewRequest(Integer rating, String content) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("평점은 1점 이상 5점 이하로 입력해야 합니다.");
        }

        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("리뷰 내용을 입력해야 합니다.");
        }
    }
    
    private void updateProductReviewStats(Product product) {

        Double averageRate =
                reviewRepository.findAverageRateByProductId(product.getId());

        Integer reviewCount =
                Math.toIntExact(reviewRepository.countByProduct_IdAndStatus(product.getId(), "ACTIVE"));

        product.updateReviewStats(
                averageRate,
                reviewCount
        );
    }
}