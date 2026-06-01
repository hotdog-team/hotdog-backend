package com.dto.project.domain.member.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MemberResponse {
    private String email;
    private String name;
    private String phone;
    private String jobType;
    private String ageRange;
    private List<Long> profileTagIds;
    private boolean isJobRecommendEnabled;

    private String zipCode;
    private String baseAddress;
    private String detailAddress;
}
