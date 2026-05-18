package com.dto.project.domain.order.repository;

import com.dto.project.domain.member.entity.Member;
import com.dto.project.domain.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    // 특정 회원의 주문 목록 조회
    List<Order> findByMemberOrderByCreatedAtDesc(Member member);

    // 주문 상세 조회 (N+1 방지)
    Optional<Order> findById(Long id);
}