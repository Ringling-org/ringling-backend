package org.ringling.backend.common.exception;

import lombok.Getter;
import org.ringling.backend.common.code.ErrorCode;

@Getter
public abstract class CustomException extends RuntimeException {

    private final ErrorCode errorCode;

    public CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
