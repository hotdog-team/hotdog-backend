package com.dto.project.domain.weighting.dto;

import com.dto.project.domain.weighting.entity.WeightLogType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MetaTagWeightLogRequest {
    private Long metaTagId;
    private WeightLogType actionType;
    private Long referenceId;
    private LocalDateTime eventTimeStamp;
    private Long stayDuration;
}
