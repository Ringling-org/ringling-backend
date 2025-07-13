package org.ringling.backend.common.dto;

import org.ringling.backend.common.code.ErrorCode;
import lombok.Getter;

@Getter
public class ApiResponse<T> {

    private final String code;
    private final String message;
    private final T data;

    private ApiResponse(String code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("SUCCESS", "요청에 성공했습니다.", data);
    }

    public static ApiResponse<?> successWithNoData() {
        return new ApiResponse<>("SUCCESS", "요청에 성공했습니다.", null);
    }

    // ErrorCode를 사용하는 새로운 error 정적 메서드
    public static ApiResponse<?> error(ErrorCode errorCode) {
        return new ApiResponse<>(errorCode.getCode(), errorCode.getMessage(), null);
    }
}
