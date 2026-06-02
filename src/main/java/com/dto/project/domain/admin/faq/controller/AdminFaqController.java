package com.dto.project.domain.admin.faq.controller;

import com.dto.project.domain.admin.faq.dto.AdminFaqCreateRequest;
import com.dto.project.domain.admin.faq.dto.AdminFaqUpdateRequest;
import com.dto.project.domain.admin.faq.service.AdminFaqService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/faqs")
@RequiredArgsConstructor
public class AdminFaqController {

    private final AdminFaqService adminFaqService;

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> createFaq(@RequestBody AdminFaqCreateRequest request) {
        adminFaqService.createFaq(request);
        return ResponseEntity.ok("FAQ 등록 완료");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> updateFaq(
            @PathVariable Long id,
            @RequestBody AdminFaqUpdateRequest request) {
        adminFaqService.updateFaq(id, request);
        return ResponseEntity.ok("FAQ 수정 완료");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> deleteFaq(@PathVariable Long id) {
        adminFaqService.deleteFaq(id);
        return ResponseEntity.ok("FAQ 삭제 완료");
    }
}