package org.ringling.backend.fcm.exception;

import org.ringling.backend.common.code.ErrorCode;
import org.ringling.backend.common.exception.CustomException;

public class FirebaseException extends CustomException {

    public FirebaseException(ErrorCode errorCode) {
        super(errorCode);
    }
}
