package com.dto.project.domain.faq.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "faqs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Faq {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FaqCategory category;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "CLOB", nullable = false)
    private String content;

    @Column(nullable = false)
    private String status = "ACTIVE";

    @Builder
    public Faq(FaqCategory category, String title, String content) {
        this.category = category;
        this.title = title;
        this.content = content;
    }

    // 관리자가 FAQ를 수정할 때 사용하는 비즈니스 메서드
    public void updateFaq(FaqCategory category, String title, String content, String status) {
        this.category = category;
        this.title = title;
        this.content = content;
        if (status != null) {
            this.status = status;
        }
    }

    // faq 삭제
    public void delete() {
        this.status = "DELETED"; // Soft Delete
    }
}