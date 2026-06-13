package com.dto.project.domain.order.controller;

import com.dto.project.domain.member.entity.Member;
import com.dto.project.domain.member.repository.MemberRepository;
import com.dto.project.domain.order.dto.CheckoutRequest;
import com.dto.project.domain.order.dto.CheckoutResponse;
import com.dto.project.domain.order.dto.OrderItemCancelRequest;
import com.dto.project.domain.order.dto.OrderItemReturnRequest;
import com.dto.project.domain.order.dto.OrderRequest;
import com.dto.project.domain.order.dto.OrderResponse;
import com.dto.project.domain.order.dto.OrderReturnRequest;
import com.dto.project.domain.order.entity.OrderStatus;
import com.dto.project.domain.order.service.OrderService;
import com.dto.project.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final MemberRepository memberRepository;
    private final SecurityUtil securityUtil;

    private Member getLoginMember() {
        Long memberId = securityUtil.resolveMemberId();

        return memberRepository.findById(memberId)
                .orElseThrow(() ->
                        new IllegalArgumentException("회원 정보를 찾을 수 없습니다.")
                );
    }

    // 구매 내역 조회
    @GetMapping
    public ResponseEntity<Page<OrderResponse>> getMyOrders(
            @RequestParam(required = false) OrderStatus status,
            @PageableDefault(size = 10, sort = "createdAt")
            Pageable pageable
    ) {
        Member member = getLoginMember();

        Page<OrderResponse> orders =
                orderService.getOrderHistory(member, status, pageable)
                        .map(orderService::toOrderResponse);

        return ResponseEntity.ok(orders);
    }

    // 상세 조회
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderDetail(
            @PathVariable Long orderId
    ) {
        Member member = getLoginMember();

        return ResponseEntity.ok(
                orderService.toOrderResponse(
                        orderService.getOrderDetail(orderId, member)
                )
        );
    }

    // 주문 취소
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<Void> cancelOrder(
            @PathVariable Long orderId
    ) {
        Member member = getLoginMember();

        orderService.cancelOrder(orderId, member);

        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{orderId}/cancel-items")
    public ResponseEntity<Void> cancelOrderItems(
            @PathVariable Long orderId,
            @RequestBody OrderItemCancelRequest request
    ) {
    	Member member = getLoginMember();

        orderService.cancelOrderItems(
                orderId,
                request.getOrderItemIds(),
                member
        );

        return ResponseEntity.ok().build();
    }

    // 장바구니 기반 주문서 조회
    @PostMapping("/checkout/cart")
    public ResponseEntity<CheckoutResponse> getCartCheckout(
            @RequestBody CheckoutRequest request
    ) {
        Member member = getLoginMember();

        CheckoutResponse response =
                orderService.createCartCheckout(request, member);

        return ResponseEntity.ok(response);
    }

    // 바로구매 기반 주문서 조회
    @PostMapping("/checkout/direct")
    public ResponseEntity<CheckoutResponse> getDirectCheckout(
            @RequestBody CheckoutRequest request
    ) {
        Member member = getLoginMember();

        CheckoutResponse response =
                orderService.createDirectCheckout(request, member);

        return ResponseEntity.ok(response);
    }

    // 주문 생성
    @PostMapping
    public ResponseEntity<Long> createOrder(
            @RequestBody OrderRequest request
    ) {
        Member member = getLoginMember();

        Long orderId = orderService.createOrder(request, member);

        return ResponseEntity.ok(orderId);
    }
    
    // 반품
    @PostMapping("/{orderId}/return-request")
    public ResponseEntity<Void> requestReturn(
            @PathVariable Long orderId,
            @RequestBody OrderReturnRequest request
    ) {

		Member member = getLoginMember();

		orderService.requestReturn(orderId, member, request);

		return ResponseEntity.ok().build();
	}
    
 // 주문 상품 부분 반품
    @PostMapping("/{orderId}/return-items")
    public ResponseEntity<Void> requestReturnItems(

            @PathVariable Long orderId,

            @RequestBody OrderItemReturnRequest request
    ) {

        Member member = getLoginMember();

        orderService.requestReturnItems(

                orderId,

                request.getOrderItemIds(),

                member,

                OrderReturnRequest.builder()
                        .reason(request.getReason())
                        .detailReason(request.getDetailReason())
                        .build()
        );

        return ResponseEntity.ok().build();
    }
    
 // 전체 반품 완료
    @PostMapping("/{orderId}/return-complete")
    public ResponseEntity<Void> completeReturn(
            @PathVariable Long orderId
    ) {
        Member member = getLoginMember();

        orderService.completeReturn(orderId, member);

        return ResponseEntity.ok().build();
    }

    // 주문 상품 부분 반품 완료
    @PostMapping("/{orderId}/return-items-complete")
    public ResponseEntity<Void> completeReturnItems(
            @PathVariable Long orderId,
            @RequestBody OrderItemReturnRequest request
    ) {
        Member member = getLoginMember();

        orderService.completeReturnItems(
                orderId,
                request.getOrderItemIds(),
                member
        );

        return ResponseEntity.ok().build();
    }
}