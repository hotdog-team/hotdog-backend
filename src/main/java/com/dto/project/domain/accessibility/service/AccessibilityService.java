package com.dto.project.domain.accessibility.service;

import com.dto.project.domain.accessibility.dto.AccessibilityResponse;
import com.dto.project.domain.accessibility.dto.AccessibilityRequest;
import com.dto.project.domain.accessibility.entity.Accessibility;
import com.dto.project.domain.accessibility.repository.AccessibilityRepository;
import com.dto.project.domain.member.repository.MemberRepository;
import com.dto.project.global.exception.DefaultErrorDetailMessages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class AccessibilityService {

    @Autowired
    AccessibilityRepository accessibilityRepository;

    @Autowired
    MemberRepository memberRepository;

    @Transactional
    public AccessibilityResponse getMySettings() {
        Long memberId = resolveMemberId();
        Accessibility entity = accessibilityRepository.findByMemberId(memberId)
                .orElseGet(() -> defaultEntity(memberId));
        if (entity.getId() == null) {
            entity = accessibilityRepository.save(entity);
        }
        return AccessibilityResponse.from(entity);
    }

    @Transactional
    public AccessibilityResponse updateMySettings(AccessibilityRequest request) {
        Long memberId = resolveMemberId();
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

    private Long resolveMemberId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, DefaultErrorDetailMessages.LOGIN_REQUIRED);
        }
        String email = auth.getName();
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "회원 정보를 찾을 수 없습니다."))
                .getId();
    }
}
