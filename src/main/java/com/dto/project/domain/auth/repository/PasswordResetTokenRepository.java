package com.dto.project.domain.auth.repository;

import com.dto.project.domain.auth.entity.PasswordResetToken;
import com.dto.project.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);

    // 기존 토큰 삭제용
    void deleteByMember(Member member);
}