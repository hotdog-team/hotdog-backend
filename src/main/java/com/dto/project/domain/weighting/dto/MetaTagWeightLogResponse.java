package com.dto.project.domain.weighting.dto;

import com.dto.project.domain.weighting.entity.MetaTagWeightLog;
import com.dto.project.domain.weighting.entity.WeightLogType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MetaTagWeightLogResponse {
    private Long id;
    private Long memberId;
    private Long metaTagId;
    private WeightLogType actionType;
    private Double appliedWeight;
    private Long referenceId;
    private LocalDateTime eventTimeStamp;
    private LocalDateTime createdAt;

    public static MetaTagWeightLogResponse from(MetaTagWeightLog entity){
        return MetaTagWeightLogResponse.builder()
                .id(entity.getId())
                .memberId(entity.getMemberId())
                .metaTagId(entity.getMetaTagId())
                .actionType(entity.getActionType())
                .appliedWeight(entity.getAppliedWeight())
                .referenceId(entity.getReferenceId())
                .eventTimeStamp(entity.getEventTimeStamp())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
