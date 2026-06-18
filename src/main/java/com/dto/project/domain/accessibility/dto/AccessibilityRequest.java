package com.dto.project.domain.accessibility.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccessibilityRequest {
    private int fontSizeStep;
    private boolean highContrastEnabled;
    private boolean screenReaderOptimized;
}
