package org.ringling.backend.common.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    UNEXPECTED_ERROR("CM001", "시스템 오류가 발생했습니다. 잠시 후 다시 시도해주세요."),

    SIGNUP_REQUIRED("AU001", "회원가입이 필요한 사용자입니다."),
    EXISTS_USER("AU002", "이미 등록된 사용자입니다."),
    LOGOUT_ERROR("AU003", "로그아웃에 실패했습니다. 다시 시도해주세요."),
    REFRESH_TOKEN_NOT_FOUND("AU004", "Refresh 토큰이 존재하지 않아 인증을 수행할 수 없습니다."),
    REFRESH_TOKEN_INVALID("AU005", "유효하지 않은 Refresh 토큰입니다."),
    USER_NOT_FOUND("AU006", "해당 유저를 찾을 수 없습니다."),
    ACCESS_TOKEN_NOT_FOUND("AU007", "Access 토큰이 존재하지 않아 인증을 수행할 수 없습니다."),
    ACCESS_TOKEN_INVALID("AU008", "유효하지 않은 Access 토큰입니다."),

    INVALID_SNAP_URL("SN001", "유효하지 않은 URL 입니다."),

    INVALID_REMINDER_TIME("RN001", "알림 시간은 현재 시간 이후로만 설정할 수 있습니다."),
    ALREADY_EXISTS_REMINDER("RN002", "이미 등록된 알림입니다."),

    FIREBASE_SERVICE_ERROR("FB000", "알림 서비스 처리 중 오류가 발생했습니다."),
    INVALID_FCM_TOKEN("FB001", "잘못된 FCM 토큰 형식입니다."),
    UNREGISTERED_FCM_TOKEN("FB002", "등록되지 않았거나 만료된 FCM 토큰입니다."),
    ;
    private final String code;
    private final String message;
}