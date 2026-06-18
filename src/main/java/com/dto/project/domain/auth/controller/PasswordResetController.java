package com.dto.project.domain.auth.controller;

import com.dto.project.domain.auth.dto.PasswordResetConfirmRequest;
import com.dto.project.domain.auth.dto.PasswordResetRequest;
import com.dto.project.domain.auth.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth/password-reset")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    // 1단계: 재설정 링크 요청
    @PostMapping("/request")
    public ResponseEntity<Map<String, String>> requestReset(@Valid @RequestBody PasswordResetRequest request) {
        passwordResetService.requestPasswordReset(request.getEmail());

        return ResponseEntity.ok(Map.of("message", "입력하신 이메일로 재설정 링크가 발송되었습니다."));
    }

    // 2단계: 새 비밀번호 확정
    @PostMapping("/confirm")
    public ResponseEntity<Map<String, String>> confirmReset(@Valid @RequestBody PasswordResetConfirmRequest request) {
        passwordResetService.confirmPasswordReset(request.getToken(), request.getNewPassword());

        return ResponseEntity.ok(Map.of("message", "비밀번호가 성공적으로 변경되었습니다. 다시 로그인해 주세요."));
    }
}