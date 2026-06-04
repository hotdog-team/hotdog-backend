package com.dto.project.domain.order.controller;

import com.dto.project.domain.member.entity.Member;
import com.dto.project.domain.member.repository.MemberRepository;
import com.dto.project.domain.order.dto.CheckoutRequest;
import com.dto.project.domain.order.dto.CheckoutResponse;
import com.dto.project.domain.order.dto.OrderRequest;
import com.dto.project.domain.order.dto.OrderResponse;
import com.dto.project.domain.order.entity.Order;
import com.dto.project.domain.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final MemberRepository memberRepository;

    //구매 내역 조회
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getMyOrders(
            @RequestParam Long memberId) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

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
    
 // 장바구니 기반 주문서 조회
    @PostMapping("/checkout/cart")
    public ResponseEntity<CheckoutResponse> getCartCheckout(
            Authentication authentication,
            @RequestBody CheckoutRequest request
    ) {
    	Long memberId = Long.valueOf(authentication.getName());

    	Member member = memberRepository.findById(memberId)
    	        .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));
        CheckoutResponse response = orderService.createCartCheckout(request, member);

        return ResponseEntity.ok(response);
    }

    // 바로구매 기반 주문서 조회
    @PostMapping("/checkout/direct")
    public ResponseEntity<CheckoutResponse> getDirectCheckout(
            Authentication authentication,
            @RequestBody CheckoutRequest request
    ) {
        Long memberId = Long.valueOf(authentication.getName());

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        CheckoutResponse response = orderService.createDirectCheckout(request, member);

        return ResponseEntity.ok(response);
    }

    //프론트엔드에서 결제/주문 요청을 받는 엔드포인트
    @PostMapping
    public ResponseEntity<Long> createOrder(
            @RequestParam Long memberId,
            @RequestBody OrderRequest request) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        Long orderId = orderService.createOrder(request, member);
        return ResponseEntity.ok(orderId);
    }
}