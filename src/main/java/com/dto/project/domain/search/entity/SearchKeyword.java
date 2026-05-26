package com.dto.project.domain.search.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "search_keywords")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SearchKeyword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String keyword;

    @Column(name = "search_count", nullable = false)
    private Integer searchCount;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public SearchKeyword(String keyword) {
        this.keyword = keyword;
        this.searchCount = 1;
        this.updatedAt = LocalDateTime.now();
    }

    public void increaseCount() {
        this.searchCount += 1;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void addCount(Integer count) {
        this.searchCount += count;
        this.updatedAt = LocalDateTime.now();
    }
}