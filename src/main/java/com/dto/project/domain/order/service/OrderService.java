package com.dto.project.domain.order.service;

import com.dto.project.domain.member.entity.Member;
import com.dto.project.domain.order.dto.OrderRequest;
import com.dto.project.domain.order.entity.Order;
import com.dto.project.domain.order.entity.OrderItem;
import com.dto.project.domain.order.entity.ProductSource;
import com.dto.project.domain.order.repository.OrderRepository;
import com.dto.project.domain.product.entity.Product;
import com.dto.project.domain.product.repository.ProductRepository;
import com.dto.project.global.exception.DefaultErrorDetailMessages;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    // ==========================================
    // 하이브리드 주문 생성 로직 (배송비 & 재고 차감 완비)
    // ==========================================
    @Transactional
    public Long createOrder(OrderRequest request, Member member) {

        // 1. Order 엔티티 기본 세팅
        Order order = Order.builder()
                .member(member)
                .receiverName(request.getReceiverName())
                .receiverPhone(request.getReceiverPhone())
                .deliveryAddress(request.getDeliveryAddress())
                .requestMessage(request.getRequestMessage())
                .totalAmount(request.getTotalAmount())
                .paymentMethod(request.getPaymentMethod())
                .deliveryFee(request.getDeliveryFee())
                .build();

        // 2. OrderItem 리스트 순회하며 매핑
        for (OrderRequest.OrderItemDto itemDto : request.getOrderItems()) {

            OrderItem.OrderItemBuilder itemBuilder = OrderItem.builder()
                    .source(itemDto.getSource())
                    .quantity(itemDto.getQuantity())
                    .priceAtOrder(itemDto.getPrice());

            // 출처에 따른 분기 처리
            if (itemDto.getSource() == ProductSource.INTERNAL) {
                // 사내 상품 유효성 검증 및 세팅
                Product product = productRepository.findById(itemDto.getProductId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사내 상품을 찾을 수 없습니다."));

                // 사내 상품 재고 차감 로직 실행 (Product 엔티티의 비즈니스 메서드 호출)
                // 만약 재고 부족 시 여기서 IllegalArgumentException이 터지며 트랜잭션이 롤백
                product.decreaseStock(itemDto.getQuantity());

                itemBuilder.product(product);

            } else if (itemDto.getSource() == ProductSource.NAVER) {
                // 네이버 상품 스냅샷 데이터 세팅
                itemBuilder.naverProductId(itemDto.getNaverProductId())
                        .productName(itemDto.getProductName())
                        .imageUrl(itemDto.getImageUrl())
                        .category(itemDto.getCategory())
                        .description(itemDto.getDescription());
            }

            // 양방향 연관관계 편의 메서드 호출
            order.addOrderItem(itemBuilder.build());
        }

        // 3. DB 저장 및 반환
        orderRepository.save(order);
        return order.getId();
    }


    // 주문 목록 조회
    public List<Order> getOrderHistory(Member member) {
        return orderRepository.findByMemberOrderByCreatedAtDesc(member);
    }

    // 주문 상세 조회
    public Order getOrderDetail(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, DefaultErrorDetailMessages.NOT_FOUND));
    }

    // 주문 취소 로직
    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = getOrderDetail(orderId);
        order.cancel();

    }
}