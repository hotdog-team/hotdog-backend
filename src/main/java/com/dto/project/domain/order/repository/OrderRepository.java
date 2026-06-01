package com.dto.project.domain.order.repository;

import com.dto.project.domain.member.entity.Member;
import com.dto.project.domain.order.entity.Order;
import com.dto.project.domain.order.entity.OrderStatus;
import com.dto.project.domain.order.entity.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    // 특정 회원의 주문 목록 조회
    List<Order> findByMemberOrderByCreatedAtDesc(Member member);

    // 주문 상세 조회 (N+1 방지)
    Optional<Order> findById(Long id);

    //  3일 경과된 특정 상태, 특정 결제수단의 주문 목록 조회
    @Query("SELECT o FROM Order o WHERE o.createdAt <= :targetTime AND o.status = :status AND o.paymentMethod = :paymentMethod")
    List<Order> findExpiredPendingOrders(
            @Param("targetTime") LocalDateTime targetTime,
            @Param("status") OrderStatus status,
            @Param("paymentMethod") PaymentMethod paymentMethod
    );
}