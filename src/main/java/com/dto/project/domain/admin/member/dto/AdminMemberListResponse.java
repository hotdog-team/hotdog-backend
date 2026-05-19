package com.dto.project.domain.admin.member.dto;

import com.dto.project.domain.member.entity.Member;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class AdminMemberListResponse {
    private Long id;              // 회원 고유 번호
    private String email;         // 회원 계정
    private String name;          // 회원 이름
    private String role;          // 현재 권한
    private String status;        // 현재 상태
    private LocalDateTime createdAt; // 가입 일시

    public AdminMemberListResponse(Member member) {
        this.id = member.getId();
        this.email = member.getEmail();
        this.name = member.getName();
        this.role = member.getRole().name();
        this.status = member.getStatus().name();
        this.createdAt = member.getCreatedAt();
    }
}