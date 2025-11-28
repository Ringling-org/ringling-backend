package org.ringling.backend.snap.exception;

import org.ringling.backend.common.code.ErrorCode;
import org.ringling.backend.common.exception.CustomException;

public class SnapException extends CustomException {

    public SnapException(ErrorCode errorCode) {
        super(errorCode);
    }
}
