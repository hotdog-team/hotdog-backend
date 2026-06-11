package com.dto.project.domain.order.dto;

import com.dto.project.domain.order.entity.Order;
import com.dto.project.domain.order.entity.OrderItem;
import com.dto.project.domain.order.entity.ProductSource;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class OrderResponse {
    private Long orderId;
    private Long orderItemId;
    private String orderStatus;     // ORDERED, CANCELLED, SHIPPED
    private int totalPrice;         // 총 결제 금액
    private LocalDateTime orderDate; // 주문 일시

    // 프론트엔드 렌더링용 대표 상품 매핑 필드
    private String productName;
    private String imageUrl;
    private String category;
    private String description;
    // 배송/결제 정보
    private String receiverName;
    private String receiverPhone;
    private String deliveryAddress;
    private String requestMessage;
    private int deliveryFee;
    private String paymentMethod;

    // 다건 주문 상품 목록
    private List<OrderItemResponse> orderItems;

    public OrderResponse(Order order) {
        this.orderId = order.getId();
        this.orderStatus = order.getStatus().name();
        this.totalPrice = order.getTotalAmount();
        this.orderDate = order.getCreatedAt();
        this.receiverName = order.getReceiverName();
        this.receiverPhone = order.getReceiverPhone();
        this.deliveryAddress = order.getDeliveryAddress();
        this.requestMessage = order.getRequestMessage();
        this.deliveryFee = order.getDeliveryFee();
        
        if (order.getPaymentMethod() != null) {
            this.paymentMethod = order.getPaymentMethod().name();
        }

        if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
            OrderItem firstItem = order.getOrderItems().get(0);

            // 기존 대표 상품 필드 유지
            this.orderItemId = firstItem.getId();

            if (firstItem.getSource() == ProductSource.INTERNAL && firstItem.getProduct() != null) {
                this.productName = firstItem.getProduct().getName();
                this.imageUrl = firstItem.getImageUrl();
                this.category = firstItem.getCategory();
                this.description = firstItem.getProduct().getShortDescription();

            } else if (firstItem.getSource() == ProductSource.NAVER) {
                this.productName = firstItem.getProductName();
                this.imageUrl = firstItem.getImageUrl();
                this.category = firstItem.getCategory();
                this.description = firstItem.getDescription();
            }

            // 다건 주문 상품 목록 추가
            this.orderItems = order.getOrderItems()
                    .stream()
                    .map(OrderItemResponse::new)
                    .toList();
        }
    }

    @Getter
    public static class OrderItemResponse {
        private Long orderItemId;
        private Long productId;
        private String productName;
        private String imageUrl;
        private String category;
        private String description;
        private int quantity;
        private int priceAtOrder;
        private String source;
        private String status;

        public OrderItemResponse(OrderItem orderItem) {
            this.orderItemId = orderItem.getId();
            this.quantity = orderItem.getQuantity();
            this.priceAtOrder = orderItem.getPriceAtOrder();
            this.source = orderItem.getSource().name();
            this.status = orderItem.getStatus().name();

            if (orderItem.getSource() == ProductSource.INTERNAL && orderItem.getProduct() != null) {
                this.productId = orderItem.getProduct().getId();
                this.productName = orderItem.getProduct().getName();
                this.imageUrl = orderItem.getImageUrl();
                this.category = orderItem.getCategory();
                this.description = orderItem.getProduct().getShortDescription();

            } else if (orderItem.getSource() == ProductSource.NAVER) {
                this.productName = orderItem.getProductName();
                this.imageUrl = orderItem.getImageUrl();
                this.category = orderItem.getCategory();
                this.description = orderItem.getDescription();
            }
        }
    }
}