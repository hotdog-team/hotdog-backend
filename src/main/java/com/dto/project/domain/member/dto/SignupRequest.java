package com.dto.project.domain.member.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class SignupRequest {

    // 1. 기본 필수 정보
    @NotBlank(message = "이메일은 필수 입력값입니다.")
    @Email(message = "올바른 이메일 형식이어야 합니다.")
    private String email;

    @NotBlank(message = "비밀번호는 필수 입력값입니다.")
    @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하로 입력해주세요.")
    private String password;

    @NotBlank(message = "이름은 필수 입력값입니다.")
    private String name;

    // 2. 인적사항 및 취향 정보
    private String ageRange;      // 연령대
    private String jobType;       // 직종
    private Long purposeId;       // 이용 목적

    // 3. 관심사 다중 선택 (member_tag_weights 초기 설정용)
    private List<Long> selectedTagIds;

    // 가입 시 동의 여부
    @JsonProperty("isJobRecommendEnabled")
    private boolean isJobRecommendEnabled;
}