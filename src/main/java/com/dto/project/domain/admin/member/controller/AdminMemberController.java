package com.dto.project.domain.admin.member.controller;

import com.dto.project.domain.admin.member.dto.AdminMemberDetailResponse;
import com.dto.project.domain.admin.member.dto.AdminMemberListResponse;
import com.dto.project.domain.admin.member.dto.AdminMemberStatusUpdateRequest;
import com.dto.project.domain.admin.member.service.AdminMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/members")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminMemberController {

    private final AdminMemberService adminMemberService;

    /**
     * [관리자] 회원 목록 검색 및 필터링 조회 (페이징)
     */
    @GetMapping
    public ResponseEntity<Page<AdminMemberListResponse>> getMembers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @PageableDefault(page = 0, size = 20) Pageable pageable) {
        Page<AdminMemberListResponse> members = adminMemberService.searchMembers(keyword, status, pageable);
        return ResponseEntity.ok(members);
    }

    /**
     * [관리자] 단일 회원 상세 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<AdminMemberDetailResponse> getMemberDetail(@PathVariable Long id) {
        AdminMemberDetailResponse memberDetail = adminMemberService.getMemberDetail(id);
        return ResponseEntity.ok(memberDetail);
    }

    /**
     * [관리자] 회원 상태 및 권한 변경 (정지/탈퇴 시 즉시 로그아웃 연동)
     */
    @PatchMapping("/{id}")
    public ResponseEntity<Void> updateMemberStatus(
            @PathVariable Long id,
            @RequestBody AdminMemberStatusUpdateRequest request) {
        adminMemberService.updateMemberStatus(id, request);
        return ResponseEntity.ok().build();
    }
}