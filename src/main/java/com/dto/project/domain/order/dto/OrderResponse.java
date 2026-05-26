package com.dto.project.domain.order.dto;

import com.dto.project.domain.order.entity.Order;
import com.dto.project.domain.order.entity.OrderItem;
import com.dto.project.domain.order.entity.ProductSource;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class OrderResponse {
    private Long orderId;
    private String orderStatus;     // ORDERED, CANCELED, SHIPPED
    private int totalPrice;         // 총 결제 금액
    private LocalDateTime orderDate; // 주문 일시

    // 프론트엔드 렌더링용 매핑 필드
    private String productName;
    private String imageUrl;
    private String category;
    private String description;

    public OrderResponse(Order order) {
        this.orderId = order.getId();
        this.orderStatus = order.getStatus().name();
        this.totalPrice = order.getTotalAmount();
        this.orderDate = order.getCreatedAt();

        // 첫 번째 상품 정보를 대표 정보로 매핑
        if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
            OrderItem firstItem = order.getOrderItems().get(0);

            if (firstItem.getSource() == ProductSource.INTERNAL && firstItem.getProduct() != null) {
                // 사내 상품: 연관된 Product 엔티티에서 실시간(또는 캐시된) 정보를 가져옴
                this.productName = firstItem.getProduct().getName();

                // 주의: Product 엔티티에 imageUrl이 없다면 별도 처리 필요. 일단 altText로 대체 예시
                this.imageUrl = firstItem.getProduct().getAltText();

                // 카테고리 정보 조회 로직 보강 필요 (현재 Product에 categoryId만 있으므로 조인 또는 별도 주입 필요)
                this.category = "사내상품"; // 임시 처리, 실제로는 카테고리 엔티티 연동
                this.description = firstItem.getProduct().getShortDescription();

            } else if (firstItem.getSource() == ProductSource.NAVER) {
                // 네이버 상품: 주문 당시 저장해둔 스냅샷 데이터를 그대로 사용
                this.productName = firstItem.getProductName();
                this.imageUrl = firstItem.getImageUrl();
                this.category = firstItem.getCategory();
                this.description = firstItem.getDescription();
            }
        }
    }
}