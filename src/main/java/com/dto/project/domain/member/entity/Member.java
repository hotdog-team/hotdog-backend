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

    @Column(name = "age_range", length = 50)
    private String ageRange;

    @Column(name = "job_type", length = 100)
    private String jobType;

    // 💡 기획 의도에 따라 lifestyleTag 제거
    // 향후 MetaTag 엔티티와 별도 매핑 테이블을 통해 8가지 분류(Category, Seasonal, Purpose 등) 관리 예정
    @Column(name = "purpose_id")
    private Long purposeId;

    @Builder.Default
    @Column(name = "is_job_recommend_enabled", nullable = false)
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

    // ==========================================
    // 비즈니스 로직 메서드
    // ==========================================

    /**
     * 회원 프로필 수정
     */
    public void updateProfile(String name, String jobType, Long purposeId, Boolean isJobRecommendEnabled) {
        if (name != null) this.name = name;
        if (jobType != null) this.jobType = jobType;
        if (purposeId != null) this.purposeId = purposeId;
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
}


