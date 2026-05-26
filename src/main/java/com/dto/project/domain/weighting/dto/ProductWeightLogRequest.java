package com.dto.project.domain.weighting.dto;

import com.dto.project.domain.weighting.entity.WeightLogStatus;
import com.dto.project.domain.weighting.entity.WeightLogType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductWeightLogRequest {
    private Long productId;
    private WeightLogType actionType;
    private Long referenceId;
    private LocalDateTime eventTimeStamp;
    private WeightLogStatus status;
    private Long stayDuration;
}
