package com.dto.project.domain.accessibility.dto;

import com.dto.project.domain.accessibility.entity.Accessibility;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AccessibilityResponse {
    private Long id;
    private Long memberId;
    private int fontSizeStep;
    private boolean highContrastEnabled;
    private boolean screenReaderOptimized;
    private LocalDateTime updatedAt;

    public static AccessibilityResponse from(Accessibility entity) {
        return AccessibilityResponse.builder()
                .id(entity.getId())
                .memberId(entity.getMemberId())
                .fontSizeStep(entity.getFontSizeStep())
                .highContrastEnabled(entity.isHighContrastEnabled())
                .screenReaderOptimized(entity.isScreenReaderOptimized())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
