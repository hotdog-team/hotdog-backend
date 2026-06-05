package com.dto.project.domain.member.controller;

import com.dto.project.domain.member.dto.MemberResponse;
import com.dto.project.domain.member.dto.MemberUpdateRequest;
import com.dto.project.domain.member.dto.PasswordUpdateRequest;
import com.dto.project.domain.member.service.MemberService;
import com.dto.project.global.util.SecurityUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final SecurityUtil securityUtil;

    // 내 정보 수정 API
    @PatchMapping("/me")
    public ResponseEntity<Void> updateProfile(@RequestBody MemberUpdateRequest request) {
        Long memberId = securityUtil.resolveMemberId();
        memberService.updateProfile(memberId, request);
        return ResponseEntity.ok().build();
    }

    // 내 정보 조회 API
    @GetMapping("/me")
    public ResponseEntity<MemberResponse> getMyProfile() {
        Long memberId = securityUtil.resolveMemberId();
        return ResponseEntity.ok(memberService.getProfile(memberId));
    }

    // 회원 탈퇴 API
    @DeleteMapping("/me")
    public ResponseEntity<Void> withdraw(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        String accessToken = (bearerToken != null && bearerToken.startsWith("Bearer ")) ? bearerToken.substring(7) : null;

        Long memberId = securityUtil.resolveMemberId();
        memberService.withdraw(memberId, accessToken);
        return ResponseEntity.ok().build();
    }

    // 비밀번호 변경 API
    @PatchMapping("/me/password")
    public ResponseEntity<Void> updatePassword(@RequestBody PasswordUpdateRequest request) {
        Long memberId = securityUtil.resolveMemberId();
        memberService.updatePassword(memberId, request);
        return ResponseEntity.ok().build();
    }
}