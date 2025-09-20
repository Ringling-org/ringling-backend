package org.ringling.backend.remindernotification.exception;

import org.ringling.backend.common.code.ErrorCode;
import org.ringling.backend.common.exception.CustomException;

public class ReminderNotificationException extends CustomException {

    public ReminderNotificationException(ErrorCode errorCode) {
        super(errorCode);
    }
}
