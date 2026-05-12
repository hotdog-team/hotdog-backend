package com.dto.project.domain.accessibility.controller;

import com.dto.project.domain.accessibility.dto.AccessibilityResponse;
import com.dto.project.domain.accessibility.dto.AccessibilityRequest;
import com.dto.project.domain.accessibility.service.AccessibilityService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accessibility")
public class AccessibilityController {

    private final AccessibilityService accessibilityService;

    public AccessibilityController(AccessibilityService accessibilityService) {
        this.accessibilityService = accessibilityService;
    }

    @GetMapping
    public AccessibilityResponse getMySettings() {
        return accessibilityService.getMySettings();
    }

    @PutMapping
    public AccessibilityResponse updateMySettings(@RequestBody AccessibilityRequest request) {
        return accessibilityService.updateMySettings(request);
    }
}
