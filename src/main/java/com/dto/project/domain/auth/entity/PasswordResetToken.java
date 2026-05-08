package com.dto.project.domain.auth.entity;

import com.dto.project.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "password_reset_tokens")
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    // 회원이 삭제되면 토큰도 삭제되거나, 불필요한 조인을 막기 위해 LAZY 적용.
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    // 만료 여부 확인 편의 메서드
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }
}