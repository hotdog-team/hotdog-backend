package com.dto.project.domain.weighting.dto;

import com.dto.project.domain.weighting.entity.ProductWeightLog;
import com.dto.project.domain.weighting.entity.WeightLogStatus;
import com.dto.project.domain.weighting.entity.WeightLogType;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class ProductWeightLogResponse {
    private Long id;
    private Long memberId;
    private Long productId;
    private WeightLogType actionType;
    private Integer appliedWeight;
    private Long referenceId;
    private LocalDateTime eventTimeStamp;
    private LocalDateTime createdAt;
    private WeightLogStatus status;

    public static ProductWeightLogResponse from(ProductWeightLog entity){
        return ProductWeightLogResponse.builder()
                .id(entity.getId())
                .memberId(entity.getMemberId())
                .productId(entity.getProductId())
                .actionType(entity.getActionType())
                .appliedWeight(entity.getAppliedWeight())
                .referenceId(entity.getReferenceId())
                .eventTimeStamp(entity.getEventTimeStamp())
                .createdAt(entity.getCreatedAt())
                .status(entity.getStatus())
                .build();

    }
}
