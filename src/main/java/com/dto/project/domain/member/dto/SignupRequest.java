package com.dto.project.domain.member.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
public class SignupRequest {

    // 1. 기본 필수 정보
    private String email;
    private String password;
    private String name;

    // 2. 인적사항 및 취향 정보
    private String ageRange;      // 연령대
    private String jobType;       // 직종
    private String lifestyleTag;  // 대표 라이프스타일 테마

    // 3. 관심사 다중 선택 (member_tag_weights 초기 설정용)
    private List<Long> lifestyleTagIds;
}
