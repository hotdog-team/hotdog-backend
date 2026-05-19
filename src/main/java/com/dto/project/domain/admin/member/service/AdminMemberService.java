package com.dto.project.domain.admin.member.service;

import com.dto.project.domain.admin.member.dto.AdminMemberDetailResponse;
import com.dto.project.domain.admin.member.dto.AdminMemberListResponse;
import com.dto.project.domain.admin.member.dto.AdminMemberStatusUpdateRequest;
import com.dto.project.domain.auth.service.AuthService;
import com.dto.project.domain.member.entity.Member;
import com.dto.project.domain.member.entity.MemberStatus;
import com.dto.project.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminMemberService {

    private final MemberRepository memberRepository;
    private final AuthService authService;

    public Page<AdminMemberListResponse> searchMembers(String keyword, String status, Pageable pageable) {
        MemberStatus memberStatus = null;
        if (status != null && !status.isBlank()) {
            try {
                memberStatus = MemberStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "유효하지 않은 상태 필터입니다.");
            }
        }
        return memberRepository.searchMembers(keyword, memberStatus, pageable)
                .map(AdminMemberListResponse::new);
    }

    public AdminMemberDetailResponse getMemberDetail(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 회원을 찾을 수 없습니다."));
        return new AdminMemberDetailResponse(member);
    }

    @Transactional
    public void updateMemberStatus(Long id, AdminMemberStatusUpdateRequest request) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 회원을 찾을 수 없습니다."));

        member.changeRoleAndStatus(request.getRole(), request.getStatus());

        // 정지/탈퇴 시 Redis 토큰 즉시 무효화
        if ("SUSPENDED".equalsIgnoreCase(request.getStatus()) || "WITHDRAWN".equalsIgnoreCase(request.getStatus())) {
            authService.forceLogout(member.getEmail());
        }
    }
}