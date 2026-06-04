package com.dto.project.domain.auth.service;

import com.dto.project.domain.auth.dto.AuthResponse;
import com.dto.project.domain.auth.dto.LoginRequest;
import com.dto.project.domain.auth.jwt.JwtProvider;
import com.dto.project.domain.member.dto.SignupRequest;
import com.dto.project.domain.member.entity.Member;
import com.dto.project.domain.member.entity.MemberRole;
import com.dto.project.domain.member.entity.MemberStatus;
import com.dto.project.domain.member.repository.MemberRepository;
import com.dto.project.domain.weighting.service.MemberTagWeightService;
import com.dto.project.global.exception.DefaultErrorDetailMessages;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final MemberTagWeightService memberTagWeightService;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final StringRedisTemplate redisTemplate;

    // 1. 회원가입
    @Transactional
    public void signup(SignupRequest request) {
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, DefaultErrorDetailMessages.DUPLICATED_VALUES);
        }

        try {
            Member member = memberRepository.save(Member.builder()
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .name(request.getName())
                    .ageRange(request.getAgeRange())
                    .jobType(request.getJobType())
                    .isJobRecommendEnabled(request.getIsJobRecommendEnabled())
                    .role(MemberRole.ROLE_USER)
                    .status(MemberStatus.ACTIVE)
                    .build());

            memberTagWeightService.initializeFromSignup(member, request.getProfileTagIds());

        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "회원가입 처리 중 오류가 발생했습니다.", e);
        }
    }

    // 2. 로그인
    @Transactional
    public AuthResponse login(LoginRequest request) {
        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, DefaultErrorDetailMessages.LOGIN_FAILED));

        // 계정 상태 방어 로직 (탈퇴/정지 유저 차단)
        if (member.getStatus() != MemberStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, DefaultErrorDetailMessages.NO_PERMISSION);
        }

        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, DefaultErrorDetailMessages.LOGIN_FAILED);
        }

        String accessToken = jwtProvider.createAccessToken(member.getId(), member.getEmail(), member.getRole().name());
        String refreshToken = jwtProvider.createRefreshToken(member.getEmail());

        // Redis에 Refresh Token 저장 (7일 유지)
        redisTemplate.opsForValue().set("RT:" + member.getEmail(), refreshToken, 7, TimeUnit.DAYS);

        return new AuthResponse(accessToken, refreshToken, member.getId(), member.getEmail(), member.getName());
    }

    // 3. 로그아웃
    @Transactional
    public void logout(String accessToken) {
        try {
            String email = jwtProvider.getEmail(accessToken);

            // Redis에서 Refresh Token 삭제
            redisTemplate.delete("RT:" + email);

            // Access Token 남은 시간 계산 후 블랙리스트에 올림
            Long expiration = jwtProvider.getExpiration(accessToken);
            redisTemplate.opsForValue().set(accessToken, "logout", expiration, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "로그아웃 처리 중 오류가 발생했습니다.");
        }
    }

    // 4. 토큰 재발급
    @Transactional
    public AuthResponse reissue(String refreshToken) {
        // 1. 들어온 Refresh Token이 유효한지 검증
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않은 Refresh Token 입니다.");
        }

        // 2. 토큰에서 이메일 추출 후 유저 조회
        String email = jwtProvider.getEmail(refreshToken);
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, DefaultErrorDetailMessages.NOT_FOUND));

        // 3. Redis에 저장된 진짜 Refresh Token과 비교
        String redisRefreshToken = redisTemplate.opsForValue().get("RT:" + email);
        if (redisRefreshToken == null || !redisRefreshToken.equals(refreshToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그아웃 되었거나 일치하지 않는 토큰입니다. 다시 로그인해주세요.");
        }

        // 4. 계정 상태 한 번 더 검증
        if (member.getStatus() != MemberStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, DefaultErrorDetailMessages.NO_PERMISSION);
        }

        // 5. 모든 검증 통과 시 새로운 토큰 세트 발급
        String newAccessToken = jwtProvider.createAccessToken(member.getId(), member.getEmail(), member.getRole().name());
        String newRefreshToken = jwtProvider.createRefreshToken(member.getEmail());

        // 6. Redis 정보 업데이트
        redisTemplate.opsForValue().set("RT:" + member.getEmail(), newRefreshToken, 7, TimeUnit.DAYS);

        return new AuthResponse(newAccessToken, newRefreshToken, member.getId(), member.getEmail(), member.getName());
    }

    // 5. [관리자용] 특정 유저 강제 로그아웃
    @Transactional
    public void forceLogout(String email) {
        redisTemplate.delete("RT:" + email);
    }
}