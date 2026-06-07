package com.dto.project.domain.admin.order.controller;

import com.dto.project.domain.admin.order.dto.AdminOrderResponse;
import com.dto.project.domain.admin.order.dto.AdminOrderUpdateRequest;
import com.dto.project.domain.admin.order.service.AdminOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    // 관리자 주문 목록 조회
    @GetMapping
    public Page<AdminOrderResponse> getOrders(
            @RequestParam(required = false) String status,
            Pageable pageable
    ) {
        return adminOrderService.getOrders(status, pageable);
    }

    // 관리자 주문 상세 조회
    @GetMapping("/{orderId}")
    public AdminOrderResponse getOrder(@PathVariable Long orderId) {
        return adminOrderService.getOrder(orderId);
    }

    // 관리자 배송 상태 및 운송장 번호 수정
    @PatchMapping("/{orderId}")
    public AdminOrderResponse updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody AdminOrderUpdateRequest request
    ) {
        return adminOrderService.updateOrderStatus(orderId, request);
    }
}