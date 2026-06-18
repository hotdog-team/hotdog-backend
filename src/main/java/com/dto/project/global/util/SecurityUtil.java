package com.dto.project.global.util;

import com.dto.project.global.exception.DefaultErrorDetailMessages;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Component
public class SecurityUtil {

    public Long resolveMemberId() {
        return resolveMemberIdOptional()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, DefaultErrorDetailMessages.LOGIN_REQUIRED));
    }

    public Optional<Long> resolveMemberIdOptional() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return Optional.empty();
        }

        Object principal = auth.getPrincipal();
        if (!(principal instanceof String value) || value.isBlank()) {
            return Optional.empty();
        }

        try {
            return Optional.of(Long.parseLong(value.trim()));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}