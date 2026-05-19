package com.dto.project.domain.order.controller;

import com.dto.project.domain.member.entity.Member;
import com.dto.project.domain.order.dto.OrderResponse;
import com.dto.project.domain.order.entity.Order;
import com.dto.project.domain.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    //구매 내역 조회
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getMyOrders(
            @AuthenticationPrincipal Member member) {
        List<Order> orders = orderService.getOrderHistory(member);
        return ResponseEntity.ok(orders.stream().map(OrderResponse::new).collect(Collectors.toList()));
    }

    //상세 조회
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderDetail(@PathVariable Long orderId) {
        return ResponseEntity.ok(new OrderResponse(orderService.getOrderDetail(orderId)));
    }

    //주문 취소
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long orderId) {
        orderService.cancelOrder(orderId);
        return ResponseEntity.ok().build();
    }
}