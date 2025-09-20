package org.ringling.backend.remindernotification.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RegisterReminderNotificationRequest {

    Integer snapId;
    LocalDateTime notificationTime;
}
