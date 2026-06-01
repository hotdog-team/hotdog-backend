package com.dto.project.domain.payment.entity;

import com.dto.project.domain.order.entity.Order;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @Column(name = "payment_key", unique = true)
    private String paymentKey;

    @Column(name = "payment_method", length = 100)
    private String paymentMethod; // CARD, CASH 등

    @Column(name = "amount")
    private Integer amount;

    @Column(name = "status", length = 50)
    private String status; // PENDING, PROCESSING, CANCELED, COMPLETED, FAILED

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Builder
    public Payment(Order order, String paymentKey, String paymentMethod, Integer amount, String status, LocalDateTime approvedAt) {
        this.order = order;
        this.paymentKey = paymentKey;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.status = status;
        this.approvedAt = approvedAt;
    }

    public void updateStatus(String status) {
        this.status = status;
    }
}