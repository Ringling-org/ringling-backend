package org.ringling.backend.common.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 예시: SAMPLE_ERROR("E001", "샘플 에러입니다."),
    ;

    private final String code;
    private final String message;
}
