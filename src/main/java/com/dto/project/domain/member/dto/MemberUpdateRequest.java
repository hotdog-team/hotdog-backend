package com.dto.project.domain.member.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
public class MemberUpdateRequest {
    private String name;
    private String phone;
    private String ageRange; // 연령대
    private String zipCode;       // 우편번호
    private String baseAddress;   // 기본 주소
    private String detailAddress; // 상세 주소

    private String jobType;
    private Long purposeId;                 // 단일 선택 항목 (이용 목적)
    private List<Long> merchandisingTagIds; // 다중 선택 항목 (선호도/기획의도)

    @JsonProperty("isJobRecommendEnabled")
    private Boolean isJobRecommendEnabled;
}