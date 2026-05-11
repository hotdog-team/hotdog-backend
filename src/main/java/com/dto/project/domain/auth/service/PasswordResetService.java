package com.dto.project.domain.auth.service;

import com.dto.project.domain.auth.entity.PasswordResetToken;
import com.dto.project.domain.auth.repository.PasswordResetTokenRepository;
import com.dto.project.domain.member.entity.Member;
import com.dto.project.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final MemberRepository memberRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService; // EmailService 의존성 주입

    @Transactional
    public void requestPasswordReset(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 회원을 찾을 수 없습니다."));

        // 1. 기존에 이 회원에게 발급된 토큰이 있다면 삭제
        tokenRepository.deleteByMember(member);

        // 2. 호출 후 flush 강제
        tokenRepository.flush();

        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .member(member)
                .expiryDate(LocalDateTime.now().plusMinutes(30))
                .build();

        tokenRepository.save(resetToken);

        String resetLink = "http://localhost:5173/reset-password/" + token;
        emailService.sendPasswordResetEmail(member.getEmail(), resetLink);
    }
    @Transactional
    public void confirmPasswordReset(String token, String newRawPassword) {
        // 토큰 조회
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "유효하지 않은 비밀번호 재설정 토큰입니다."));

        // 만료 여부 검사
        if (resetToken.isExpired()) {
            tokenRepository.delete(resetToken);
            throw new ResponseStatusException(HttpStatus.GONE, "만료된 토큰입니다. 다시 요청해 주세요.");
        }

        Member member = resetToken.getMember();

        // 기존 비밀번호와 동일한지 체크
        if (passwordEncoder.matches(newRawPassword, member.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "새 비밀번호는 이전 비밀번호와 다르게 설정해야 합니다.");
        }

        // 새 비밀번호 업데이트
        member.updatePassword(passwordEncoder.encode(newRawPassword));

        // 토큰 삭제
        tokenRepository.delete(resetToken);
        log.info("회원 ID {}의 비밀번호가 성공적으로 재설정되었습니다.", member.getId());
    }
}