package com.dto.project.domain.weighting.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name="product_weight_logs")
public class ProductWeightLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="member_id", nullable = false)
    private Long memberId;

    @Column(name="product_id", nullable = false)
    private Long productId;

    @Column(name="action_type")
    @Enumerated(EnumType.STRING)
    private WeightLogType actionType;
    @Column(name="applied_weight")
    private Double appliedWeight;
    @Column(name="reference_id")
    private Long referenceId;
    @Column(name="event_timestamp")
    private LocalDateTime eventTimeStamp;
    @Column(name="created_at")
    private LocalDateTime createdAt;
}
