package com.dto.project.domain.review.repository;

import com.dto.project.domain.member.entity.Member;
import com.dto.project.domain.order.entity.OrderItem;
import com.dto.project.domain.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // 1. [사용자] 특정 상품의 상태별(ACTIVE) 리뷰 목록 조회
    List<Review> findAllByProductIdAndStatusOrderByCreatedAtDesc(Long productId, String status);

    // 2. [사용자] 내 상태별(ACTIVE) 리뷰 목록 조회 (추가됨!)
    List<Review> findAllByMemberAndStatusOrderByCreatedAtDesc(Member member, String status);

    // 3. [공통] 해당 주문건(OrderItem)으로 작성된 리뷰가 이미 존재하는지 체크
    boolean existsByOrderItem(OrderItem orderItem);

}