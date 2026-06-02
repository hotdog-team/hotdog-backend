package com.dto.project.domain.admin.faq.service;

import com.dto.project.domain.admin.faq.dto.AdminFaqCreateRequest;
import com.dto.project.domain.admin.faq.dto.AdminFaqUpdateRequest;
import com.dto.project.domain.faq.entity.Faq;
import com.dto.project.domain.faq.repository.FaqRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminFaqService {

    private final FaqRepository faqRepository;

    // 1. FAQ 등록
    @Transactional
    public Long createFaq(AdminFaqCreateRequest request) {
        Faq faq = Faq.builder()
                .category(request.getCategory())
                .title(request.getTitle())
                .content(request.getContent())
                .build();
        return faqRepository.save(faq).getId();
    }

    // 2. FAQ 수정
    @Transactional
    public void updateFaq(Long faqId, AdminFaqUpdateRequest request) {
        Faq faq = faqRepository.findById(faqId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 FAQ입니다."));

        faq.updateFaq(
                request.getCategory(),
                request.getTitle(),
                request.getContent(),
                request.getStatus()
        );
    }

    // 3. FAQ 삭제 (Soft Delete)
    @Transactional
    public void deleteFaq(Long faqId) {
        Faq faq = faqRepository.findById(faqId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 FAQ입니다."));

        faq.delete(); // 상태를 DELETED로 변경
    }
}