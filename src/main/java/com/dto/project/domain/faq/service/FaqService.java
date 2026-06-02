package com.dto.project.domain.faq.service;

import com.dto.project.domain.faq.dto.FaqResponse;
import com.dto.project.domain.faq.entity.FaqCategory;
import com.dto.project.domain.faq.repository.FaqRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FaqService {

    private final FaqRepository faqRepository;

    public List<FaqResponse> getFaqs(FaqCategory category) {
        if (category == null) {
            return faqRepository.findByStatus("ACTIVE").stream()
                    .map(FaqResponse::new).collect(Collectors.toList());
        }
        return faqRepository.findByCategoryAndStatus(category, "ACTIVE").stream()
                .map(FaqResponse::new).collect(Collectors.toList());
    }
}