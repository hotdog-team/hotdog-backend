package com.dto.project.domain.member.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
public class MemberUpdateRequest {
    private String name;
    private String jobType;
    private Long purposeId;                 // 단일 선택 항목 (이용 목적)
    private List<Long> merchandisingTagIds; // 다중 선택 항목 (선호도/기획의도)

    @JsonProperty("isJobRecommendEnabled")
    private Boolean isJobRecommendEnabled;
}