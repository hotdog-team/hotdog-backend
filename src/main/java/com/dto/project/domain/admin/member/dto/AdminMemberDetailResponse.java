package com.dto.project.domain.admin.member.dto;

import com.dto.project.domain.member.entity.Member;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class AdminMemberDetailResponse {
    private Long id;
    private String email;
    private String name;
    private String role;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public AdminMemberDetailResponse(Member member) {
        this.id = member.getId();
        this.email = member.getEmail();
        this.name = member.getName();
        this.role = member.getRole().name();
        this.status = member.getStatus().name();
        this.createdAt = member.getCreatedAt();
        this.updatedAt = member.getUpdatedAt();
    }
}