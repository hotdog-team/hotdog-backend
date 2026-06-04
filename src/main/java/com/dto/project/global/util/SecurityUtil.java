package com.dto.project.global.util;

import com.dto.project.domain.member.repository.MemberRepository;
import com.dto.project.global.exception.DefaultErrorDetailMessages;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
public class SecurityUtil {
    private final MemberRepository memberRepository;

    public Long resolveMemberId() {
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
