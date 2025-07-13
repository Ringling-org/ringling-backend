package org.ringling.backend.common.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    SIGNUP_REQUIRED("AU001", "회원가입이 필요한 사용자입니다."),
    EXISTS_USER("AU002", "이미 등록된 사용자입니다."),
    LOGOUT_ERROR("AU003", "로그아웃에 실패했습니다. 다시 시도해주세요."),
    ;
    private final String code;
    private final String message;
}