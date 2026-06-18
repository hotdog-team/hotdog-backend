package com.dto.project.domain.order.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dto.project.domain.order.entity.OrderItem;
import com.dto.project.domain.order.entity.OrderItemStatus;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long>{
	
	// 주문 ID로 주문 상품 목록 조회
    List<OrderItem> findByOrderId(Long orderId);
    
    // 주문 ID와 주문 상품 ID 목록으로 부분 취소 대상 조회
    List<OrderItem> findByOrderIdAndIdIn(Long orderId, List<Long> orderItemIds);

    // 특정 상태의 주문 상품 존재 여부 확인
    boolean existsByOrderIdAndStatus(Long orderId, OrderItemStatus status);
}