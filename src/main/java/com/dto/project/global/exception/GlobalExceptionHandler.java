package com.dto.project.global.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.util.StringUtils;
import org.springframework.jdbc.CannotGetJdbcConnectionException;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    //400 INVALID_ARGUMENT - 유효성 검증
    // DTO에서 @NotBlank(message=...), @Min/@Max(message=...) 등으로 문구를 정하고, 여기서는 defaultMessage만 노출합니다. 전역에서 Min/Max를 해석하지 않습니다.
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

        logClientError(e, request, code, code.getHttpStatus().value());
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
        logClientError(e, request, code, code.getHttpStatus().value());
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
                DefaultErrorDetailMessages.missingRequiredParameter(e.getParameterName()),
                request.getRequestURI(),
                errors
        );
        logClientError(e, request, code, code.getHttpStatus().value());
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
        logClientError(e, request, code, code.getHttpStatus().value());
        return ResponseEntity.status(code.getHttpStatus()).body(response);
    }

    // 401 UNAUTHENTICATED - 인증 실패/로그인 필요
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException e, HttpServletRequest request) {
        ErrorCode code = ErrorCode.UNAUTHENTICATED;
        String detail;
        if (StringUtils.hasText(e.getMessage())) {
            detail = e.getMessage();
        } else if (e instanceof BadCredentialsException) {
            detail = DefaultErrorDetailMessages.LOGIN_FAILED;
        } else {
            detail = DefaultErrorDetailMessages.LOGIN_REQUIRED;
        }
        ErrorResponse response = ErrorResponse.from(
                code,
                detail,
                request.getRequestURI()
        );
        logClientError(e, request, code, code.getHttpStatus().value());
        return ResponseEntity.status(code.getHttpStatus()).body(response);
    }

    // 403 PERMISSION_DENIED - 권한 없음
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException e, HttpServletRequest request) {
        ErrorCode code = ErrorCode.PERMISSION_DENIED;
        String detail = StringUtils.hasText(e.getMessage())
                ? e.getMessage()
                : DefaultErrorDetailMessages.NO_PERMISSION;
        ErrorResponse response = ErrorResponse.from(
                code,
                detail,
                request.getRequestURI()
        );
        logClientError(e, request, code, code.getHttpStatus().value());
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
                    DefaultErrorDetailMessages.unknownError(ErrorCode.INTERNAL.name()),
                    uri
            );
            logServerError(e, request, ErrorCode.INTERNAL, ErrorCode.INTERNAL.getHttpStatus().value());
            return ResponseEntity.status(ErrorCode.INTERNAL.getHttpStatus()).body(response);
        }

        ErrorCode code = switch (http) {
            case BAD_REQUEST -> ErrorCode.INVALID_ARGUMENT;
            case UNAUTHORIZED -> ErrorCode.UNAUTHENTICATED;
            case FORBIDDEN -> ErrorCode.PERMISSION_DENIED;
            case NOT_FOUND -> ErrorCode.NOT_FOUND;
            case CONFLICT -> ErrorCode.ABORTED;
            case UNPROCESSABLE_ENTITY -> ErrorCode.VALIDATION_FAILED;
            case TOO_MANY_REQUESTS -> ErrorCode.RESOURCE_EXHAUSTED;
            case SERVICE_UNAVAILABLE -> ErrorCode.UNAVAILABLE;
            default -> ErrorCode.INTERNAL;
        };

        if (code == ErrorCode.INTERNAL) {
            String detail = StringUtils.hasText(e.getReason())
                    ? e.getReason()
                    : DefaultErrorDetailMessages.unknownError(ErrorCode.INTERNAL.name());
            ErrorResponse response = ErrorResponse.from(ErrorCode.INTERNAL, detail, uri);
            logServerError(e, request, ErrorCode.INTERNAL, ErrorCode.INTERNAL.getHttpStatus().value());
            return ResponseEntity.status(ErrorCode.INTERNAL.getHttpStatus()).body(response);
        }

        String detail = switch (http) {
            case NOT_FOUND -> StringUtils.hasText(e.getReason()) ? e.getReason() : DefaultErrorDetailMessages.NOT_FOUND;
            case SERVICE_UNAVAILABLE -> StringUtils.hasText(e.getReason()) ? e.getReason() : DefaultErrorDetailMessages.DB_UNAVAILABLE;
            default -> StringUtils.hasText(e.getReason())
                    ? e.getReason()
                    : http.getReasonPhrase();
        };

        ErrorResponse response = ErrorResponse.from(code, detail, uri);
        logByHttpSeries(e, request, code, http);
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
        logClientError(e, request, code, code.getHttpStatus().value());
        return ResponseEntity.status(code.getHttpStatus()).body(response);
    }

    // 409 ALREADY_EXISTS - 유니크 제약 위반 등
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException e, HttpServletRequest request) {
        ErrorCode code = ErrorCode.ALREADY_EXISTS;
        ErrorResponse response = ErrorResponse.from(
                code,
                DefaultErrorDetailMessages.DUPLICATED_VALUES,
                request.getRequestURI()
        );
        logClientError(e, request, code, code.getHttpStatus().value());
        return ResponseEntity.status(code.getHttpStatus()).body(response);
    }

    // 503 UNAVAILABLE - DB 연결 실패 등
    @ExceptionHandler({
            CannotGetJdbcConnectionException.class,
            DataAccessResourceFailureException.class
    })
    public ResponseEntity<ErrorResponse> handleDataAccessResourceFailure(Exception e, HttpServletRequest request) {
        ErrorCode code = ErrorCode.UNAVAILABLE;
        ErrorResponse response = ErrorResponse.from(
                code,
                DefaultErrorDetailMessages.DB_UNAVAILABLE,
                request.getRequestURI()
        );
        logServerError(e, request, code, code.getHttpStatus().value());
        return ResponseEntity.status(code.getHttpStatus()).body(response);
    }

    // 그 외
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e, HttpServletRequest request) {
        ErrorCode code = ErrorCode.INTERNAL;
        ErrorResponse response = ErrorResponse.from(
                code,
                DefaultErrorDetailMessages.unknownError(ErrorCode.INTERNAL.name()),
                request.getRequestURI()
        );
        logServerError(e, request, code, code.getHttpStatus().value());
        return ResponseEntity.status(code.getHttpStatus()).body(response);
    }

    //4NN대 에러에 대한 로그를 서버에 남깁니다.
    private void logClientError(Throwable ex, HttpServletRequest request, ErrorCode errorCode, int httpStatus) {
        log.warn("클라이언트 에러: path:{}, httpStatus:{}, errorCode:{}, type:{}, message:{}",
                request.getRequestURI(),
                httpStatus,
                errorCode.name(),
                ex.getClass().getSimpleName(),
                ex.getMessage());
    }

    //5NN대 에러에 대한 로그를 서버에 남깁니다.
    private void logServerError(Throwable ex, HttpServletRequest request, ErrorCode errorCode, int httpStatus) {
        log.error("서버 에러: path:{}, httpStatus:{}, errorCode:{}, type:{}, message:{}",
                request.getRequestURI(),
                httpStatus,
                errorCode.name(),
                ex.getClass().getSimpleName(),
                ex.getMessage(),
                ex);
    }

    //예외 번호에 따라 400번 혹은 500번대로 보냅니다.
    private void logByHttpSeries(Throwable ex, HttpServletRequest request, ErrorCode errorCode, HttpStatus http) {
        int status = http.value();
        if (status >= 500) {
            logServerError(ex, request, errorCode, status);
        } else {
            logClientError(ex, request, errorCode, status);
        }
    }

}
