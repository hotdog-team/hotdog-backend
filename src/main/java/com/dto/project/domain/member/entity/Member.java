package com.dto.project.domain.member.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "members")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 50)
    private String phone;

    @Column(name = "age_range", length = 50)
    private String ageRange;

    @Column(name = "job_type", length = 100)
    private String jobType;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean isJobRecommendEnabled = true;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private MemberRole role = MemberRole.ROLE_USER;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(length = 30, nullable = false)
    private MemberStatus status = MemberStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    // ==========================================
    // 비즈니스 로직 메서드
    // ==========================================

    /**
     * 회원 프로필 수정
     */
    public void updateProfile(String name, String phone, String ageRange, String jobType, Boolean isJobRecommendEnabled) {
        if (name != null) this.name = name;
        if (phone != null) this.phone = phone;
        if (ageRange != null) this.ageRange = ageRange;
        if (jobType != null) this.jobType = jobType;
        if (isJobRecommendEnabled != null) this.isJobRecommendEnabled = isJobRecommendEnabled;
    }

    /**
     * 회원 탈퇴
     */
    public void withdraw() {
        this.status = MemberStatus.WITHDRAWN;
    }

    public void updateName(String name) {
        this.name = name;
    }

    /**
     * 비밀번호 수정
     */
    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }

    public void changeRoleAndStatus(String role, String status) {
        try {
            if (role != null && !role.isBlank()) {
                this.role = MemberRole.valueOf(role.toUpperCase());
            }
            if (status != null && !status.isBlank()) {
                this.status = MemberStatus.valueOf(status.toUpperCase());
            }
            this.updatedAt = LocalDateTime.now();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 권한 또는 상태 값입니다.");
        }
    }

    // 로그인 성공 시 호출하여 마지막 접속 시간을 현재로 갱신하는 메서드
    public void updateLastLoginAt() {
        this.lastLoginAt = LocalDateTime.now();
    }
}


