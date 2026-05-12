package com.dto.project.global.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    //사용되지 않는 변수 e는 오류 확인 가독성을 위해 놔두었습니다, 후 수정될 수 있습니다.
    //400 INVALID_ARGUMENT - 유효성 검증
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        FieldError firstError = null;

        for (FieldError fe : e.getBindingResult().getFieldErrors()) {
            if (firstError == null) {
                firstError = fe;
            }
            errors.put(fe.getField(), fe.getDefaultMessage());
        }

        ErrorCode code = ErrorCode.INVALID_ARGUMENT;
        String detail = (firstError != null && firstError.getDefaultMessage() != null)
                ? firstError.getDefaultMessage()
                : "입력값을 확인해 주세요.";

        ErrorResponse response = ErrorResponse.from(
                code,
                detail,
                request.getRequestURI(),
                errors
        );

        return ResponseEntity.status(code.getHttpStatus()).body(response);
    }

    // 400 INVALID_ARGUMENT - JSON 파싱 실패/형식 오류
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException e, HttpServletRequest request) {
        ErrorCode code = ErrorCode.INVALID_ARGUMENT;
        ErrorResponse response = ErrorResponse.from(
                code,
                "요청 형식이 올바르지 않습니다.",
                request.getRequestURI()
        );
        return ResponseEntity.status(code.getHttpStatus()).body(response);
    }

    // 400 FAILED_PRECONDITION - 필수 파라미터 누락
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameter(MissingServletRequestParameterException e, HttpServletRequest request) {
        ErrorCode code = ErrorCode.FAILED_PRECONDITION;
        Map<String, String> errors = new HashMap<>();
        errors.put(e.getParameterName(), "필수 항목입니다.");
        ErrorResponse response = ErrorResponse.from(
                code,
                "필수 항목이 누락되었습니다.",
                request.getRequestURI(),
                errors
        );
        return ResponseEntity.status(code.getHttpStatus()).body(response);
    }

    // 400 INVALID_ARGUMENT - 파라미터 타입 불일치
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException e,
            HttpServletRequest request
    ) {
        ErrorCode code = ErrorCode.INVALID_ARGUMENT;
        ErrorResponse response = ErrorResponse.from(
                code,
                "요청 값 형식이 올바르지 않습니다.",
                request.getRequestURI()
        );
        return ResponseEntity.status(code.getHttpStatus()).body(response);
    }

    // 401 UNAUTHENTICATED - 인증 실패/로그인 필요
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException e, HttpServletRequest request) {
        ErrorCode code = ErrorCode.UNAUTHENTICATED;
        String detail = StringUtils.hasText(e.getMessage())
                ? e.getMessage()
                : "로그인이 필요합니다.";
        ErrorResponse response = ErrorResponse.from(
                code,
                detail,
                request.getRequestURI()
        );
        return ResponseEntity.status(code.getHttpStatus()).body(response);
    }

    // 403 PERMISSION_DENIED - 권한 없음
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException e, HttpServletRequest request) {
        ErrorCode code = ErrorCode.PERMISSION_DENIED;
        String detail = StringUtils.hasText(e.getMessage())
                ? e.getMessage()
                : "해당 권한이 없습니다.";
        ErrorResponse response = ErrorResponse.from(
                code,
                detail,
                request.getRequestURI()
        );
        return ResponseEntity.status(code.getHttpStatus()).body(response);
    }

    // ResponseStatusException - 401, 404 등의 처리(누락되어 수정했습니다)
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(ResponseStatusException e, HttpServletRequest request) {
        String uri = request.getRequestURI();
        int raw = e.getStatusCode().value();
        HttpStatus http = HttpStatus.resolve(raw);

        if (http == null) {
            ErrorResponse response = ErrorResponse.from(
                    ErrorCode.INTERNAL,
                    "알 수 없는 오류가 발생했습니다.",
                    uri
            );
            return ResponseEntity.status(ErrorCode.INTERNAL.getHttpStatus()).body(response);
        }

        ErrorCode code = switch (http) {
            case BAD_REQUEST -> ErrorCode.INVALID_ARGUMENT;
            case UNAUTHORIZED -> ErrorCode.UNAUTHENTICATED;
            case FORBIDDEN -> ErrorCode.PERMISSION_DENIED;
            case NOT_FOUND -> ErrorCode.NOT_FOUND;
            case CONFLICT -> ErrorCode.ABORTED;
            default -> ErrorCode.INTERNAL;
        };

        if (code == ErrorCode.INTERNAL) {
            String detail = StringUtils.hasText(e.getReason())
                    ? e.getReason()
                    : "알 수 없는 오류가 발생했습니다.";
            ErrorResponse response = ErrorResponse.from(ErrorCode.INTERNAL, detail, uri);
            return ResponseEntity.status(ErrorCode.INTERNAL.getHttpStatus()).body(response);
        }

        String detail = switch (http) {
            case NOT_FOUND -> StringUtils.hasText(e.getReason()) ? e.getReason() : "찾는 결과가 없습니다.";
            default -> StringUtils.hasText(e.getReason())
                    ? e.getReason()
                    : http.getReasonPhrase();
        };

        ErrorResponse response = ErrorResponse.from(code, detail, uri);
        return ResponseEntity.status(http).body(response);
    }

    // IllegalArgumentException - 비즈니스 규칙 등 (팀: 409 ABORTED)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e, HttpServletRequest request) {
        ErrorCode code = ErrorCode.ABORTED;
        String detail = StringUtils.hasText(e.getMessage())
                ? e.getMessage()
                : "요청을 처리할 수 없습니다.";
        ErrorResponse response = ErrorResponse.from(code, detail, request.getRequestURI());
        return ResponseEntity.status(code.getHttpStatus()).body(response);
    }

    // 409 ALREADY_EXISTS - 유니크 제약 위반 등
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException e, HttpServletRequest request) {
        ErrorCode code = ErrorCode.ALREADY_EXISTS;
        ErrorResponse response = ErrorResponse.from(
                code,
                "이미 존재하는 값입니다.",
                request.getRequestURI()
        );
        return ResponseEntity.status(code.getHttpStatus()).body(response);
    }

    // 그 외
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e, HttpServletRequest request) {
        ErrorCode code = ErrorCode.INTERNAL;
        ErrorResponse response = ErrorResponse.from(
                code,
                "알 수 없는 오류가 발생했습니다.",
                request.getRequestURI()
        );
        return ResponseEntity.status(code.getHttpStatus()).body(response);
    }

}
