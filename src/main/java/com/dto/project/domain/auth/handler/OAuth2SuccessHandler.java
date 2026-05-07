package com.dto.project.domain.auth.handler;

import com.dto.project.domain.auth.jwt.JwtProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // 이메일 추출 (SuccessHandler에서는 간단하게 처리)
        String email = getEmailFromAttributes(attributes);
        String role = "USER";

        // 1. JWT 토큰 생성
        String accessToken = jwtProvider.createAccessToken(email, role);
        String refreshToken = jwtProvider.createRefreshToken(email);

        // 2. 프론트엔드 리다이렉트 URL 생성 (토큰을 쿼리 파라미터로 전달)
        // 프론트엔드 포트(5173)와 성공 페이지 경로 확인 필요
        String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:5173/social-success")
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private String getEmailFromAttributes(Map<String, Object> attributes) {
        if (attributes.containsKey("email")) return (String) attributes.get("email");
        if (attributes.containsKey("kakao_account")) {
            return (String) ((Map<String, Object>) attributes.get("kakao_account")).get("email");
        }
        if (attributes.containsKey("response")) {
            return (String) ((Map<String, Object>) attributes.get("response")).get("email");
        }
        return null;
    }
}