package com.dto.project.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PasswordResetConfirmRequest {
    @NotBlank(message = "유효하지 않은 요청입니다. (토큰 누락)")
    private String token;

    @NotBlank(message = "새 비밀번호를 입력해 주세요.")
    private String newPassword;
}