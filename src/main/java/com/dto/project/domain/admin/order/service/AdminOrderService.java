package com.dto.project.domain.admin.order.service;

import com.dto.project.domain.admin.order.dto.AdminOrderResponse;
import com.dto.project.domain.admin.order.dto.AdminOrderUpdateRequest;
import com.dto.project.domain.order.entity.Order;
import com.dto.project.domain.order.entity.OrderStatus;
import com.dto.project.domain.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminOrderService {

    private final OrderRepository orderRepository;

    // 관리자 주문 목록 조회
    // status 값이 있으면 상태별 조회, 없으면 전체 조회
    public Page<AdminOrderResponse> getOrders(String status, Pageable pageable) {

        if (status == null || status.isBlank()) {
            return orderRepository.findAll(pageable)
                    .map(AdminOrderResponse::from);
        }

        OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());

        return orderRepository.findByStatusOrderByCreatedAtDesc(orderStatus, pageable)
                .map(AdminOrderResponse::from);
    }

    // 관리자 주문 상세 조회
    public AdminOrderResponse getOrder(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

        return AdminOrderResponse.from(order);
    }

    // 관리자 배송 상태 및 운송장 번호 수정
    @Transactional
    public AdminOrderResponse updateOrderStatus(Long orderId, AdminOrderUpdateRequest request) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            OrderStatus status = OrderStatus.valueOf(request.getStatus().toUpperCase());
            order.updateStatus(status);
        }

        if (request.getTrackingNumber() != null && !request.getTrackingNumber().isBlank()) {
            order.updateTrackingNumber(request.getTrackingNumber());
        }

        return AdminOrderResponse.from(order);
    }
}