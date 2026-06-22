package com.dto.project.domain.auth.handler;

import com.dto.project.domain.auth.jwt.JwtProvider;
import com.dto.project.domain.member.entity.Member;
import com.dto.project.domain.member.entity.MemberStatus;
import com.dto.project.domain.member.repository.MemberRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;
    private final MemberRepository memberRepository;
    private final StringRedisTemplate redisTemplate;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        // 1. OIDC(구글) 및 일반 OAuth2(카카오, 네이버) 모두 대응하기 위해 Token에서 직접 Provider 추출
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        String provider = oauthToken.getAuthorizedClientRegistrationId();

        OAuth2User oAuth2User = oauthToken.getPrincipal();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // 2. Provider별 고유 ID 및 이메일 추출
        String providerId = null;
        String email = (String) attributes.get("email"); // CustomOAuth2UserService에서 넘겨준 이메일

        if ("google".equals(provider)) {
            providerId = String.valueOf(attributes.get("sub"));
        } else if ("naver".equals(provider)) {
            Map<String, Object> res = (Map<String, Object>) attributes.get("response");
            providerId = String.valueOf(res.get("id"));
        } else if ("kakao".equals(provider)) {
            providerId = String.valueOf(attributes.get("id"));
        }

        // 3. 탈퇴 유저 30일 쿨링오프 제어 (Redis 확인)
        if (email != null && Boolean.TRUE.equals(redisTemplate.hasKey("withdrawing_member:" + email))) {
            System.out.println("탈퇴 대기 중인 소셜 계정 접근 차단: " + email);
            // 프론트엔드 로그인 페이지로 에러 파라미터를 달아서 강제 이동
            String errorUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/login")
                    .queryParam("error", "cooling_off")
                    .build().toUriString();
            getRedirectStrategy().sendRedirect(request, response, errorUrl);
            return;
        }

        // 4. 중복 데이터 에러 방지 로직
        List<Member> memberList = memberRepository.findAllByProviderAndProviderId(provider, providerId);
        Optional<Member> memberOpt = memberList.isEmpty() ? Optional.empty() : Optional.of(memberList.get(0));

        String targetUrl;

        if (memberOpt.isEmpty()) {
            // 1. DB에 없는 소셜 계정 -> 최초 진입 (회원가입 창으로 보내기 위한 설정)
            System.out.println("신규 소셜 회원 유입 -> 회원가입 페이지 리다이렉트 준비");
            targetUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/social-success")
                    .queryParam("isNewUser", "true")
                    .queryParam("provider", provider)
                    .queryParam("providerId", providerId)
                    .build().toUriString();
        } else {
            // 2. 이미 연동을 완료한 기존 회원 -> 정상 로그인 (토큰 발급)
            Member member = memberOpt.get();

            if (member.getStatus() != MemberStatus.ACTIVE) {
                String errorUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/login")
                        .queryParam("error", "no_permission")
                        .build().toUriString();
                getRedirectStrategy().sendRedirect(request, response, errorUrl);
                return;
            }

            System.out.println("기존 소셜 연동 회원 로그인 성공");
            Long memberId = member.getId();
            String memberEmail = member.getEmail();
            String role = member.getRole().name();

            // JWT 토큰 생성
            String accessToken = jwtProvider.createAccessToken(memberId, memberEmail, role);
            String refreshToken = jwtProvider.createRefreshToken(memberEmail);

            redisTemplate.opsForValue().set("RT:" + memberEmail, refreshToken, 7, TimeUnit.DAYS);

            targetUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/social-success")
                    .queryParam("isNewUser", "false")
                    .queryParam("accessToken", accessToken)
                    .queryParam("refreshToken", refreshToken)
                    .queryParam("memberId", memberId)
                    .queryParam("email", memberEmail)
                    .queryParam("role", role)
                    .build().toUriString();
        }

        System.out.println("리다이렉트 타겟: " + targetUrl);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}