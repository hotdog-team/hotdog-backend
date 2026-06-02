package com.dto.project.domain.faq.controller;

import com.dto.project.domain.faq.dto.FaqResponse;
import com.dto.project.domain.faq.entity.FaqCategory;
import com.dto.project.domain.faq.service.FaqService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/faqs")
@RequiredArgsConstructor
public class FaqController {

    private final FaqService faqService;

    // FAQ 조회
    @GetMapping
    public ResponseEntity<List<FaqResponse>> getFaqs(@RequestParam(required = false) FaqCategory category) {
        return ResponseEntity.ok(faqService.getFaqs(category));
    }
}