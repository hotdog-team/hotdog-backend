package com.dto.project.domain.qna.controller;

import com.dto.project.domain.qna.dto.QnaDto;
import com.dto.project.domain.qna.service.QnaService;
import com.dto.project.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class QnaController {

    private final QnaService qnaService;
    private final SecurityUtil securityUtil;

    // 1. 1:1 문의 등록
    @PostMapping("/api/qnas")
    public ResponseEntity<Long> createQna(@RequestBody QnaDto.Request request) {

        Long memberId = securityUtil.resolveMemberId();

        Long qnaId = qnaService.createQna(memberId, request);
        return ResponseEntity.ok(qnaId);
    }

    // 2. 내 문의 내역
    @GetMapping("/api/members/me/qnas")
    public ResponseEntity<Page<QnaDto.Response>> getMyQnas(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Long memberId = securityUtil.resolveMemberId();

        Page<QnaDto.Response> response = qnaService.getMyQnas(memberId, pageable);
        return ResponseEntity.ok(response);
    }

    // 3. 문의 삭제
    @DeleteMapping("/api/qnas/{id}")
    public ResponseEntity<Void> deleteQna(@PathVariable Long id) {
        Long memberId = securityUtil.resolveMemberId();
        qnaService.deleteQna(memberId, id);
        return ResponseEntity.noContent().build();
    }
}