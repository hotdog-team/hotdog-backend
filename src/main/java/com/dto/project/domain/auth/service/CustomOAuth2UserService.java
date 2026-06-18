package com.dto.project.domain.auth.service;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        // 1. 소셜 서비스 구분 (google, kakao, naver)
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        Map<String, Object> attributes = oAuth2User.getAttributes();

        // 2. 소셜 고유 식별자(PK) 추출
        String providerId = extractProviderId(registrationId, attributes);

        // 3. DB 자동 저장 로직 삭제! 대신 핸들러로 전달하기 위해 Map에 추가
        Map<String, Object> customAttributes = new HashMap<>(attributes);
        customAttributes.put("provider", registrationId);
        customAttributes.put("providerId", providerId);

        System.out.println("소셜 인증 성공 - Provider: " + registrationId + ", ID: " + providerId);

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                customAttributes,
                userNameAttributeName
        );
    }

    private String extractProviderId(String registrationId, Map<String, Object> attributes) {
        if ("naver".equals(registrationId)) {
            Map<String, Object> response = (Map<String, Object>) attributes.get("response");
            return (String) response.get("id"); // 네이버 고유 식별자
        } else if ("kakao".equals(registrationId)) {
            return String.valueOf(attributes.get("id")); // 카카오 고유 식별자
        }
        return String.valueOf(attributes.get("sub")); // 구글 고유 식별자
    }
}