package com.dto.project.domain.member.controller;

import com.dto.project.domain.member.dto.MemberResponse;
import com.dto.project.domain.member.dto.MemberUpdateRequest;
import com.dto.project.domain.member.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    // 내 정보 수정 API
    @PatchMapping("/me")
    public ResponseEntity<Void> updateProfile(@RequestBody MemberUpdateRequest request, Principal principal) {
        memberService.updateProfile(principal.getName(), request);
        return ResponseEntity.ok().build();
    }

    // 내 정보 조회 API
    @GetMapping("/me")
    public ResponseEntity<MemberResponse> getMyProfile(Principal principal) {
        return ResponseEntity.ok(memberService.getProfile(principal.getName()));
    }

    // 회원 탈퇴 API
    @DeleteMapping("/me")
    public ResponseEntity<Void> withdraw(Principal principal, HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        String accessToken = (bearerToken != null && bearerToken.startsWith("Bearer ")) ? bearerToken.substring(7) : null;

        memberService.withdraw(principal.getName(), accessToken);
        return ResponseEntity.ok().build();
    }
}