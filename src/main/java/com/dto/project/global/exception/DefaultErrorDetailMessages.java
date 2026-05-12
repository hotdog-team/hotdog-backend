package com.dto.project.global.exception;

public final class DefaultErrorDetailMessages {

    private DefaultErrorDetailMessages() {
    }

    // NOT_FOUND 기본문구
    public static final String NOT_FOUND = "찾는 결과가 없습니다.";

    // 파라미터 누락 기본문구
    public static String missingRequiredParameter(String parameterName) {
        return "필수 항목이 누락되었습니다(" + parameterName + ")";
    }

    //권한이 없을 경우
    public static final String NO_PERMISSION = "해당 권한이 없습니다.";

    // DB 연결·리소스 연결 실패 등
    public static final String DB_UNAVAILABLE = "현재 서비스 점검 중입니다. 잠시 후 다시 이용해 주세요.";

    //로그인 실패
    public static final String LOGIN_FAILED = "아이디 또는 비밀번호가 일치하지 않습니다. 다시 확인해 주세요.";
    //인증
    public static final String LOGIN_REQUIRED = "로그인이 필요합니다.";

    public static final String DUPLICATED_VALUES = "이미 존재하는 값입니다";

    //그 외 오류
    public static String unknownError(String errorCode) {
        return "알 수 없는 오류가 발생했습니다. (에러 코드: " + errorCode + ")";
    }

}
