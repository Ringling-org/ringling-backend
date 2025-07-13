package org.ringling.backend.common.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    SIGNUP_REQUIRED("AU001", "회원가입이 필요한 사용자입니다.");

    private final String code;
    private final String message;
}