package com.dto.project.domain.member.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "members")
public class Member {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @Column(name = "lifestyle_tag", length = 100)
    private String lifestyleTag;

    @Column(length = 20)
    private String role = "USER";

    @Column(length = 30)
    private String status = "ACTIVE";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public Member(String email, String password, String name, String ageRange, String jobType, String lifestyleTag, String role, String status) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.ageRange = ageRange;
        this.jobType = jobType;
        this.lifestyleTag = lifestyleTag;

        if (role != null) this.role = role;
        if (status != null) this.status = status;
    }
}