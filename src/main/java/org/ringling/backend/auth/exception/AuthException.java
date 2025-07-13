package org.ringling.backend.auth.exception;

import org.ringling.backend.common.code.ErrorCode;
import org.ringling.backend.common.exception.CustomException;

public class AuthException extends CustomException {

    public AuthException(ErrorCode errorCode) {
        super(errorCode);
    }
}
