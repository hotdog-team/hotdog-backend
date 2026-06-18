package com.dto.project.domain.review.repository;

import com.dto.project.domain.member.entity.Member;
import com.dto.project.domain.order.entity.OrderItem;
import com.dto.project.domain.review.entity.Review;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // 1. [사용자] 특정 상품의 상태별(ACTIVE) 리뷰 목록 조회
    List<Review> findAllByProductIdAndStatusOrderByCreatedAtDesc(Long productId, String status);
    
    Page<Review> findAllByProductIdAndStatusOrderByCreatedAtDesc(
            Long productId,
            String status,
            Pageable pageable
    );

    // 2. [사용자] 내 상태별(ACTIVE) 리뷰 목록 조회
    List<Review> findAllByMemberAndStatusOrderByCreatedAtDesc(Member member, String status);
    
    Page<Review> findAllByMemberAndStatusOrderByCreatedAtDesc(
            Member member,
            String status,
            Pageable pageable
    );

    // 3. [공통] 해당 주문건(OrderItem)으로 작성된 ACTIVE 리뷰 존재 여부
    boolean existsByOrderItemAndStatus(OrderItem orderItem, String status);

    Optional<Review> findByOrderItemAndStatus(OrderItem orderItem, String status);

    Optional<Review> findByOrderItem(OrderItem orderItem);
    
    
    @Query("""
    	       SELECT COALESCE(AVG(r.rating), 0)
    	       FROM Review r
    	       WHERE r.product.id = :productId
    	       AND r.status = 'ACTIVE'
    	       """)
    	Double findAverageRateByProductId(Long productId);

    	long countByProduct_IdAndStatus(Long productId, String status);

}