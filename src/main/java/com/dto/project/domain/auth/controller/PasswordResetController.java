package com.dto.project.domain.auth.controller;

import com.dto.project.domain.auth.dto.PasswordResetConfirmRequest;
import com.dto.project.domain.auth.dto.PasswordResetRequest;
import com.dto.project.domain.auth.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    // 2단계: 재설정 링크 클릭 시 폼 띄우기 전 유효성 검증
    @GetMapping("/validate")
    public ResponseEntity<Map<String, String>> validateToken(@RequestParam("token") String token) {
        passwordResetService.validateToken(token);
        return ResponseEntity.ok(Map.of("message", "유효한 링크입니다."));
    }

    // 3단계: 새 비밀번호 확정
    @PostMapping("/confirm")
    public ResponseEntity<Map<String, String>> confirmReset(@Valid @RequestBody PasswordResetConfirmRequest request) {
        passwordResetService.confirmPasswordReset(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "비밀번호가 성공적으로 변경되었습니다. 다시 로그인해 주세요."));
    }
}