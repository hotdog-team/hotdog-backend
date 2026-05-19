/*
 * 각 code의 의미에 대해서는 팀 문서를 참고해주세요.
 */

package com.dto.project.global.exception;
import org.springframework.http.HttpStatus;

public enum ErrorCode {
    INVALID_ARGUMENT(HttpStatus.BAD_REQUEST),
    FAILED_PRECONDITION(HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED(HttpStatus.UNAUTHORIZED),
    PERMISSION_DENIED(HttpStatus.FORBIDDEN),
    NOT_FOUND(HttpStatus.NOT_FOUND),
    ALREADY_EXISTS(HttpStatus.CONFLICT),
    ABORTED(HttpStatus.CONFLICT),
    VALIDATION_FAILED(HttpStatus.UNPROCESSABLE_ENTITY),
    RESOURCE_EXHAUSTED(HttpStatus.TOO_MANY_REQUESTS),
    INTERNAL(HttpStatus.INTERNAL_SERVER_ERROR),
    DATA_LOSS(HttpStatus.INTERNAL_SERVER_ERROR),
    UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE);

    private final HttpStatus httpStatus;

    ErrorCode(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

}
