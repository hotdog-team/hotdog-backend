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

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    @Transactional
    public Long createOrder(OrderRequest request, Member member) {

        // 서버 단에서 직접 총 상품 금액을 안전하게 합산하기 위한 임시 변수 및 리스트
        int calculatedTotalProductAmount = 0;
        List<OrderItem> temporaryOrderItems = new ArrayList<>();

        for (OrderRequest.OrderItemDto itemDto : request.getOrderItems()) {

            OrderItem.OrderItemBuilder itemBuilder = OrderItem.builder()
                    .source(itemDto.getSource())
                    .quantity(itemDto.getQuantity());

            int finalPrice = 0;

            // 출처에 따른 분기 처리
            if (itemDto.getSource() == ProductSource.INTERNAL) {
                // 사내 상품 유효성 검증 및 세팅
                Product product = productRepository.findById(itemDto.getProductId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사내 상품을 찾을 수 없습니다."));

                // 사내 상품 재고 차감 로직 실행
                // 만약 재고 부족 시 IllegalArgumentException이 터지며 트랜잭션이 롤백
                product.decreaseStock(itemDto.getQuantity());

                finalPrice = (int) (product.getPrice() * (1 - (product.getDiscountRate() / 100.0)));

                itemBuilder.product(product);

            } else if (itemDto.getSource() == ProductSource.NAVER) {
                // 외부 상품은 기준 가격이 없으므로 프론트엔드가 넘겨준 가격으로 수용
                finalPrice = itemDto.getPrice();

                // 네이버 상품 스냅샷 데이터 세팅
                itemBuilder.naverProductId(itemDto.getNaverProductId())
                        .productName(itemDto.getProductName())
                        .imageUrl(itemDto.getImageUrl())
                        .category(itemDto.getCategory())
                        .description(itemDto.getDescription());
            }

            itemBuilder.priceAtOrder(finalPrice);

            // (최종 단가 * 수량)을 총 상품 금액에 안전하게 누적
            calculatedTotalProductAmount += (finalPrice * itemDto.getQuantity());

            // 빌드된 OrderItem 객체를 임시 보관
            temporaryOrderItems.add(itemBuilder.build());
        }

        // 최종 결제 금액 = (서버가 직접 계산한 총 상품 금액) + (프론트가 보낸 배송비)
        int finalTotalAmount = calculatedTotalProductAmount + request.getDeliveryFee();

        Order order = Order.builder()
                .member(member)
                .receiverName(request.getReceiverName())
                .receiverPhone(request.getReceiverPhone())
                .deliveryAddress(request.getDeliveryAddress())
                .requestMessage(request.getRequestMessage())
                .totalAmount(finalTotalAmount)
                .paymentMethod(request.getPaymentMethod())
                .deliveryFee(request.getDeliveryFee())
                .build();

        // 양방향 연관관계 편의 메서드 호출
        for (OrderItem orderItem : temporaryOrderItems) {
            order.addOrderItem(orderItem);
        }

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