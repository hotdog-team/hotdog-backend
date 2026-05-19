package com.dto.project.domain.category.dto;

import com.dto.project.domain.category.entity.Category;
import com.dto.project.domain.category.entity.CategoryStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CategoryResponse {
    private Long id;
    private String name;
    private CategoryStatus status;
    private LocalDateTime updatedAt;

    public static CategoryResponse from(Category entity){
        if (entity == null) {
            return null;
        }

        return CategoryResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .status(entity.getStatus())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
