package com.dto.project.domain.payment.service;

import com.dto.project.domain.cart.repository.CartRepository;
import com.dto.project.domain.member.entity.Member;
import com.dto.project.domain.order.entity.Order;
import com.dto.project.domain.order.entity.OrderItem;
import com.dto.project.domain.order.repository.OrderRepository;
import com.dto.project.domain.payment.entity.Payment;
import com.dto.project.domain.payment.repository.PaymentRepository;
import com.dto.project.domain.payment.dto.PaymentConfirmRequest;

import com.dto.project.external.toss.client.TossPaymentClient;
import com.dto.project.external.toss.dto.TossConfirmRequest;
import com.dto.project.external.toss.dto.TossPaymentResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final CartRepository cartRepository;
    private final TossPaymentClient tossPaymentClient;

    @Transactional
    public void processPayment(PaymentConfirmRequest request, Member member) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

        if ("CARD".equals(request.getPaymentMethod())) {
            // 1. Toss을 통해 최종 결제 승인 요청
            TossConfirmRequest tossRequest = new TossConfirmRequest(request.getPaymentKey(), request.getTossOrderId(), request.getAmount());
            TossPaymentResponse tossResponse = tossPaymentClient.confirmPayment(tossRequest);

            // 2. 승인 성공 시 상태 변경 및 기록
            decreaseStock(order);
            
            order.updateStatus("COMPLETED");
            savePaymentInfo(order, request, "COMPLETED");

            // 3. 결제 완료 후 현재 로그인한 회원의 장바구니 비우기
            cartRepository.deleteByMemberId(member.getId());
            log.info("결제 성공 처리 완료 - 회원 ID: {}, 주문 ID: {}", member.getId(), order.getId());

        } else if ("CASH".equals(request.getPaymentMethod())) {
            // 무통장 입금은 토스 승인 없이 대기 상태로 저장
            order.updateStatus("PENDING");
            savePaymentInfo(order, request, "PENDING");
            log.info("무통장 입금 주문 대기 처리 - 주문 ID: {}", order.getId());
        }
    }

    private void savePaymentInfo(Order order, PaymentConfirmRequest request, String status) {
        Payment payment = Payment.builder()
                .order(order)
                .paymentKey(request.getPaymentKey())
                .paymentMethod(request.getPaymentMethod())
                .amount(request.getAmount())
                .status(status)
                .approvedAt(status.equals("COMPLETED") ? LocalDateTime.now() : null)
                .build();
        paymentRepository.save(payment);
    }
    
 // 결제 성공 시 주문 상품 재고 차감
    private void decreaseStock(Order order) {
        for (OrderItem orderItem : order.getOrderItems()) {

            // 외부 상품 또는 상품 정보가 없는 경우 재고 차감 제외
            if (orderItem.getProduct() == null) {
                continue;
            }

            orderItem.getProduct().decreaseStock(orderItem.getQuantity());
        }
    }
}