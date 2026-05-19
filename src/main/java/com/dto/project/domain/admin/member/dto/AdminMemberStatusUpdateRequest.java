package com.dto.project.domain.admin.member.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AdminMemberStatusUpdateRequest {
    private String role;   // 변경할 권한
    private String status; // 변경할 상태
}