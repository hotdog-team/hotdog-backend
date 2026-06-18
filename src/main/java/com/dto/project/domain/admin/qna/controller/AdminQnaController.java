package com.dto.project.domain.admin.qna.controller;

import com.dto.project.domain.admin.qna.dto.AdminQnaDto;
import com.dto.project.domain.admin.qna.service.AdminQnaService;
import com.dto.project.domain.qna.entity.QnaStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/qnas")
@RequiredArgsConstructor
public class AdminQnaController {

    private final AdminQnaService adminQnaService;

    // 1. 문의 목록 조회
    @GetMapping
    public ResponseEntity<Page<AdminQnaDto.ListResponse>> getQnaList(
            @RequestParam(required = false) QnaStatus status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(adminQnaService.getQnaList(status, pageable));
    }

    // 2. 문의 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<AdminQnaDto.DetailResponse> getQnaDetail(@PathVariable Long id) {
        return ResponseEntity.ok(adminQnaService.getQnaDetail(id));
    }

    // 3. 답변 등록
    @PatchMapping("/{id}")
    public ResponseEntity<Void> answerQna(
            @PathVariable Long id,
            @RequestBody AdminQnaDto.AnswerRequest request) {
        adminQnaService.answerQna(id, request);
        return ResponseEntity.ok().build();
    }

    // 4. 부적절한 문의글 관리자 강제 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQna(@PathVariable Long id) {
        adminQnaService.softDeleteQna(id);
        return ResponseEntity.noContent().build();
    }
}