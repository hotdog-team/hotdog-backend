package com.dto.project.domain.order.service;

import com.dto.project.domain.cart.entity.Cart;
import com.dto.project.domain.cart.repository.CartRepository;
import com.dto.project.domain.member.entity.Member;
import com.dto.project.domain.order.dto.*;
import com.dto.project.domain.order.entity.*;
import com.dto.project.domain.order.repository.OrderItemRepository;
import com.dto.project.domain.order.repository.OrderRepository;
import com.dto.project.domain.product.entity.Product;
import com.dto.project.domain.product.repository.ProductRepository;
import com.dto.project.domain.review.repository.ReviewRepository;
import com.dto.project.domain.weighting.entity.WeightLogType;
import com.dto.project.domain.weighting.service.ProductWeightLogService;
import com.dto.project.global.exception.DefaultErrorDetailMessages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductWeightLogService productWeightLogService;
    private final ReviewRepository reviewRepository;

    public OrderResponse toOrderResponse(Order order) {
        return new OrderResponse(order, reviewRepository);
    }

    // 장바구니 기반 주문서 조회
    public CheckoutResponse createCartCheckout(CheckoutRequest request, Member member) {

        List<CheckoutResponse.Item> items = new ArrayList<>();
        int totalAmount = 0;

        for (Long cartId : request.getCartIds()) {
            Cart cart = cartRepository.findById(cartId)
                    .orElseThrow(() -> new IllegalArgumentException("장바구니 정보를 찾을 수 없습니다."));

            if (!cart.getMemberId().equals(member.getId())) {
                throw new IllegalArgumentException("본인의 장바구니만 주문할 수 있습니다.");
            }

            int unitPrice = cart.getPrice();
            int totalPrice = unitPrice * cart.getQuantity();

            items.add(CheckoutResponse.Item.builder()
                    .cartId(cart.getId())
                    .productId(cart.getProduct().getId())
                    .productName(cart.getProductName())
                    .imageUrl(cart.getImageUrl())
                    .quantity(cart.getQuantity())
                    .unitPrice(unitPrice)
                    .totalPrice(totalPrice)
                    .discountRate(cart.getProduct().getDiscountRate())
                    .build());

            totalAmount += totalPrice;
        }

        return CheckoutResponse.builder()
                .items(items)
                .totalAmount(totalAmount)
                .build();
    }

    // 바로구매 기반 주문서 조회
    public CheckoutResponse createDirectCheckout(CheckoutRequest request, Member member) {

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("상품 정보를 찾을 수 없습니다."));

        int quantity = request.getQuantity();

        if (quantity <= 0) {
            throw new IllegalArgumentException("수량은 1개 이상이어야 합니다.");
        }

        int unitPrice = product.getPrice();
        int totalPrice = unitPrice * quantity;

        CheckoutResponse.Item item = CheckoutResponse.Item.builder()
                .cartId(null)
                .productId(product.getId())
                .productName(product.getName())
                .imageUrl(null)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .totalPrice(totalPrice)
                .discountRate(product.getDiscountRate())
                .build();

        return CheckoutResponse.builder()
                .items(List.of(item))
                .totalAmount(totalPrice)
                .build();
    }

    // 주문 생성
    @Transactional
    public Long createOrder(OrderRequest request, Member member) {

        int calculatedTotalProductAmount = 0;
        List<OrderItem> temporaryOrderItems = new ArrayList<>();

        for (OrderRequest.OrderItemDto itemDto : request.getOrderItems()) {

            if (itemDto.getSource() == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "상품 출처(source)는 필수입니다."
                );
            }

            OrderItem.OrderItemBuilder itemBuilder = OrderItem.builder()
                    .source(itemDto.getSource())
                    .quantity(itemDto.getQuantity());

            int finalPrice = 0;

            if (itemDto.getSource() == ProductSource.INTERNAL) {
                Product product = productRepository.findById(itemDto.getProductId())
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "사내 상품을 찾을 수 없습니다."
                        ));

                product.decreaseStock(itemDto.getQuantity());

                finalPrice = (int) (product.getPrice() * (1 - (product.getDiscountRate() / 100.0)));

                String imageUrl = null;

                if (product.getImages() != null && !product.getImages().isEmpty()) {
                    imageUrl = product.getImages()
                            .get(0)
                            .getImageUrl();
                }

                itemBuilder.product(product)
                        .productName(product.getName())
                        .imageUrl(imageUrl)
                        .category(itemDto.getCategory())
                        .description(product.getShortDescription());

            } else if (itemDto.getSource() == ProductSource.NAVER) {
                finalPrice = itemDto.getPrice();

                itemBuilder.naverProductId(itemDto.getNaverProductId())
                        .productName(itemDto.getProductName())
                        .imageUrl(itemDto.getImageUrl())
                        .category(itemDto.getCategory())
                        .description(itemDto.getDescription());
            }

            itemBuilder.priceAtOrder(finalPrice);

            calculatedTotalProductAmount += finalPrice * itemDto.getQuantity();

            temporaryOrderItems.add(itemBuilder.build());
        }

        int finalTotalAmount =
                calculatedTotalProductAmount + request.getDeliveryFee();

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

        for (OrderItem orderItem : temporaryOrderItems) {
            order.addOrderItem(orderItem);
        }

        orderRepository.save(order);

        if (order.getTotalAmount() <= 0) {
            order.updateStatus(OrderStatus.COMPLETED);
            orderRepository.save(order);
        }
        for (OrderItem orderItem : temporaryOrderItems) {
            if (orderItem.getProduct() != null) {
                recordBuyBehavior(member.getId(), orderItem.getProduct().getId());
            }
        }

        if (request.getCartItemIds() != null
                && !request.getCartItemIds().isEmpty()) {

            cartRepository.deleteByIdInAndMemberId(
                    request.getCartItemIds(),
                    member.getId()
            );
        }
        
        return order.getId();
    }

    // 주문 목록 조회
    public Page<Order> getOrderHistory(
            Member member,
            OrderStatus status,
            Pageable pageable
    ) {
        if (status != null) {
            return orderRepository.findByMemberAndStatusOrderByCreatedAtDesc(
                    member,
                    status,
                    pageable
            );
        }

        return orderRepository.findByMemberOrderByCreatedAtDesc(
                member,
                pageable
        );
    }

    // 주문 상세 조회
    public Order getOrderDetail(Long orderId, Member member) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        DefaultErrorDetailMessages.NOT_FOUND
                ));

        if (!order.getMember().getId().equals(member.getId())) {
            throw new IllegalArgumentException("본인의 주문만 조회할 수 있습니다.");
        }

        return order;
    }

    // 전체 주문 취소
    @Transactional
    public void cancelOrder(Long orderId, Member member) {

        Order order = getOrderDetail(orderId, member);
        // 배송중이거나 배송완료 상태는 취소 불가
        if (order.getStatus() == OrderStatus.IN_TRANSIT || order.getStatus() == OrderStatus.DELIVERED) {
            throw new IllegalArgumentException("이미 배송 중이거나 완료된 상품은 취소할 수 없습니다.");
        }

        // 주문 상품 전체 취소
        for (OrderItem orderItem : order.getOrderItems()) {

            // 이미 취소된 상품은 제외
            if (orderItem.getStatus() == OrderItemStatus.CANCELLED) {
                continue;
            }

            // 사내 상품 재고 복구 & 판매량 복구
            if (orderItem.getProduct() != null) {
                orderItem.getProduct().increaseStock(orderItem.getQuantity());
                orderItem.getProduct().decreaseSalesCount(orderItem.getQuantity());
            }

            // 주문 상품 상태 변경
            orderItem.cancel();

            if (orderItem.getProduct() != null) {
                recordCancelBuyBehavior(member.getId(), orderItem.getProduct().getId());
            }
        }

        // 주문 상태 취소 완료
        order.cancel();
    }

    // 주문 상품 부분 취소
    @Transactional
    public void cancelOrderItems(
            Long orderId,
            List<Long> orderItemIds,
            Member member
    ) {

        Order order = getOrderDetail(orderId, member);

        // 배송중이거나 배송완료 상태는 취소 불가
        if (order.getStatus() == OrderStatus.IN_TRANSIT || order.getStatus() == OrderStatus.DELIVERED) {

            throw new IllegalArgumentException("이미 배송 중이거나 완료된 상품은 취소할 수 없습니다.");
        }

        // 취소할 주문 상품 조회
        List<OrderItem> cancelItems =
                orderItemRepository.findByOrderIdAndIdIn(orderId, orderItemIds);

        // 요청한 주문 상품이 실제 주문 상품과 일치하는지 검증
        if (cancelItems.size() != orderItemIds.size()) {
            throw new IllegalArgumentException("취소할 주문 상품 정보가 올바르지 않습니다.");
        }

        // 선택한 주문 상품 취소
        for (OrderItem orderItem : cancelItems) {

            // 이미 취소된 상품인지 검증
            if (orderItem.getStatus() == OrderItemStatus.CANCELLED) {
                throw new IllegalStateException("이미 취소된 주문 상품입니다.");
            }

            // 사내 상품 재고 복구 & 판매량 감소
            if (orderItem.getProduct() != null) {
                orderItem.getProduct().increaseStock(orderItem.getQuantity());
                orderItem.getProduct().decreaseSalesCount(orderItem.getQuantity());
            }

            // 주문 상품 상태 변경
            orderItem.cancel();

            if (orderItem.getProduct() != null) {
                recordCancelBuyBehavior(member.getId(), orderItem.getProduct().getId());
            }
        }

        // 남아있는 주문 상품이 있는지 확인
        boolean hasOrderedItem =
                orderItemRepository.existsByOrderIdAndStatus(orderId, OrderItemStatus.ORDERED);

        // 모두 취소되면 전체 취소, 일부만 취소되면 부분 취소
        if (hasOrderedItem) {
            order.updateStatus(OrderStatus.PARTIAL_CANCELLED);
        } else {
            order.cancel();
        }
    }

    private void recordBuyBehavior(Long memberId, Long productId) {
        try {
            productWeightLogService.recordBehavior(memberId, productId, WeightLogType.BUY);
        } catch (Exception e) {
            log.warn("주문 behavior log(BUY) 기록 실패: memberId={}, productId={}", memberId, productId, e);
        }
    }


 // 반품
    @Transactional
    public void requestReturn(
            Long orderId,
            Member member,
            OrderReturnRequest request
    ) {
        Order order = getOrderDetail(orderId, member);

        if (order.getStatus() != OrderStatus.DELIVERED
                && order.getStatus() != OrderStatus.PARTIAL_RETURN_REQUESTED
                && order.getStatus() != OrderStatus.PARTIAL_RETURN_COMPLETED) {
            throw new IllegalArgumentException("배송 완료 주문만 반품 신청할 수 있습니다.");
        }

        if (request.getReason() == null || request.getReason().isBlank()) {
            throw new IllegalArgumentException("반품 사유를 선택해 주세요.");
        }

        for (OrderItem orderItem : order.getOrderItems()) {
            if (orderItem.getStatus() == OrderItemStatus.ORDERED) {
                orderItem.requestReturn();
            }
        }

        order.updateStatus(OrderStatus.RETURN_REQUESTED);
    }
    
    @Transactional
    public void requestReturnItems(
            Long orderId,
            List<Long> orderItemIds,
            Member member,
            OrderReturnRequest request
    ) {

        Order order = getOrderDetail(orderId, member);

        if (order.getStatus() != OrderStatus.DELIVERED
                && order.getStatus() != OrderStatus.PARTIAL_RETURN_COMPLETED) {

            throw new IllegalArgumentException(
                    "배송 완료 주문만 반품 신청할 수 있습니다."
            );
        }

        if (request.getReason() == null || request.getReason().isBlank()) {
            throw new IllegalArgumentException(
                    "반품 사유를 선택해 주세요."
            );
        }

        List<OrderItem> returnItems =
                orderItemRepository.findByOrderIdAndIdIn(
                        orderId,
                        orderItemIds
                );

        if (returnItems.size() != orderItemIds.size()) {
            throw new IllegalArgumentException(
                    "반품할 주문 상품 정보가 올바르지 않습니다."
            );
        }

        for (OrderItem orderItem : returnItems) {

            orderItem.requestReturn();
        }

        boolean hasOrderedItem =
                orderItemRepository.existsByOrderIdAndStatus(
                        orderId,
                        OrderItemStatus.ORDERED
                );

        if (hasOrderedItem) {

            order.updateStatus(
                    OrderStatus.PARTIAL_RETURN_REQUESTED
            );

        } else {

            order.updateStatus(
                    OrderStatus.RETURN_REQUESTED
            );
        }
    }
    
 // 전체 반품 완료
    @Transactional
    public void completeReturn(Long orderId, Member member) {

        Order order = getOrderDetail(orderId, member);

        if (order.getStatus() != OrderStatus.RETURN_REQUESTED) {
            throw new IllegalArgumentException("반품 신청 상태의 주문만 반품 완료할 수 있습니다.");
        }

        for (OrderItem orderItem : order.getOrderItems()) {
            if (orderItem.getStatus() == OrderItemStatus.RETURN_REQUESTED) {
                orderItem.completeReturn();
            }
        }

        order.updateStatus(OrderStatus.RETURN_COMPLETED);
    }

    // 주문 상품 부분 반품 완료
    @Transactional
    public void completeReturnItems(
            Long orderId,
            List<Long> orderItemIds,
            Member member
    ) {

        Order order = getOrderDetail(orderId, member);

        if (order.getStatus() != OrderStatus.PARTIAL_RETURN_REQUESTED
                && order.getStatus() != OrderStatus.RETURN_REQUESTED) {
            throw new IllegalArgumentException("반품 신청 상태의 주문만 반품 완료할 수 있습니다.");
        }

        List<OrderItem> returnItems =
                orderItemRepository.findByOrderIdAndIdIn(orderId, orderItemIds);

        if (returnItems.size() != orderItemIds.size()) {
            throw new IllegalArgumentException("반품 완료 처리할 주문 상품 정보가 올바르지 않습니다.");
        }

        for (OrderItem orderItem : returnItems) {
            orderItem.completeReturn();
        }

        boolean hasReturnRequestedItem =
                orderItemRepository.existsByOrderIdAndStatus(
                        orderId,
                        OrderItemStatus.RETURN_REQUESTED
                );

        boolean hasOrderedItem =
                orderItemRepository.existsByOrderIdAndStatus(
                        orderId,
                        OrderItemStatus.ORDERED
                );

        if (!hasReturnRequestedItem && hasOrderedItem) {
            order.updateStatus(OrderStatus.PARTIAL_RETURN_COMPLETED);
        } else if (!hasReturnRequestedItem) {
            order.updateStatus(OrderStatus.RETURN_COMPLETED);
        }
    }  
    
    private void recordCancelBuyBehavior(Long memberId, Long productId) {
        try {
            productWeightLogService.recordBehavior(
                    memberId,
                    productId,
                    WeightLogType.CANCEL_BUY
            );
        } catch (Exception e) {
            log.warn(
                    "주문 behavior log(CANCEL_BUY) 기록 실패: memberId={}, productId={}",
                    memberId,
                    productId,
                    e
            );
        }
    }
}