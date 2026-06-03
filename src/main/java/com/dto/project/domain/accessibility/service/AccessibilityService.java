package com.dto.project.domain.accessibility.service;

import com.dto.project.domain.accessibility.dto.AccessibilityResponse;
import com.dto.project.domain.accessibility.dto.AccessibilityRequest;
import com.dto.project.domain.accessibility.entity.Accessibility;
import com.dto.project.domain.accessibility.repository.AccessibilityRepository;
import com.dto.project.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AccessibilityService {

    private final AccessibilityRepository accessibilityRepository;
    private final SecurityUtil securityUtil;

    @Transactional
    public AccessibilityResponse getMySettings() {
        Long memberId = securityUtil.resolveMemberId();
        Accessibility entity = accessibilityRepository.findByMemberId(memberId)
                .orElseGet(() -> defaultEntity(memberId));
        if (entity.getId() == null) {
            entity = accessibilityRepository.save(entity);
        }
        return AccessibilityResponse.from(entity);
    }

    @Transactional
    public AccessibilityResponse updateMySettings(AccessibilityRequest request) {
        Long memberId = securityUtil.resolveMemberId();
        Accessibility entity = accessibilityRepository.findByMemberId(memberId)
                .orElseGet(() -> defaultEntity(memberId));
        entity.setFontSizeStep(request.getFontSizeStep());
        entity.setHighContrastEnabled(request.isHighContrastEnabled());
        entity.setScreenReaderOptimized(request.isScreenReaderOptimized());
        entity.setUpdatedAt(LocalDateTime.now());

        Accessibility saved = accessibilityRepository.save(entity);
        return AccessibilityResponse.from(saved);
    }

    private Accessibility defaultEntity(Long memberId) {
        return Accessibility.builder()
                .memberId(memberId)
                .fontSizeStep(1)
                .highContrastEnabled(false)
                .screenReaderOptimized(false)
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
