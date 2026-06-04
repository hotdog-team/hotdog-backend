package com.dto.project.global.util;

import com.dto.project.global.exception.DefaultErrorDetailMessages;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class SecurityUtil {

    public Long resolveMemberId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, DefaultErrorDetailMessages.LOGIN_REQUIRED);
        }

        Object principal = auth.getPrincipal();
        if (!(principal instanceof String value) || value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, DefaultErrorDetailMessages.LOGIN_REQUIRED);
        }

        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, DefaultErrorDetailMessages.LOGIN_REQUIRED);
        }
    }
}