package com.dto.project.global.exception;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Builder
public class ErrorResponse {
    private String type;
    private String title;
    private int status;
    private String detail;
    private String instance;
    private String code;
    private Map<String, String> errors;
    private LocalDateTime timestamp;

    public static ErrorResponse from(ErrorCode code, String detail, String requestUri) {
        return ErrorResponse.builder()
                .type("about:blank")
                .title(code.getHttpStatus().getReasonPhrase())
                .status(code.getHttpStatus().value())
                .detail(detail)
                .instance(requestUri)
                .code(code.name())
                .errors(null)
                .timestamp(LocalDateTime.now())
                .build();
    }

    //errors ver
    public static ErrorResponse from(ErrorCode code, String detail, String requestUri, Map<String, String> errors) {
        return ErrorResponse.builder()
                .type("about:blank")
                .title(code.getHttpStatus().getReasonPhrase())
                .status(code.getHttpStatus().value())
                .detail(detail)
                .instance(requestUri)
                .code(code.name())
                .errors(errors)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
