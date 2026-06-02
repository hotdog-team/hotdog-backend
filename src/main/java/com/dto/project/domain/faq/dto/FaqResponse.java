package com.dto.project.domain.faq.dto;

import com.dto.project.domain.faq.entity.Faq;
import com.dto.project.domain.faq.entity.FaqCategory;
import lombok.Getter;

@Getter
public class FaqResponse {
    private Long id;
    private FaqCategory category;
    private String title;
    private String content;

    public FaqResponse(Faq faq) {
        this.id = faq.getId();
        this.category = faq.getCategory();
        this.title = faq.getTitle();
        this.content = faq.getContent();
    }
}