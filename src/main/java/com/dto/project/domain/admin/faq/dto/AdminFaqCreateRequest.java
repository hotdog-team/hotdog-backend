package com.dto.project.domain.admin.faq.dto;

import com.dto.project.domain.faq.entity.FaqCategory;
import lombok.Getter;

@Getter
public class AdminFaqCreateRequest {
    private FaqCategory category;
    private String title;
    private String content;
}