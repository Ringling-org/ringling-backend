package org.ringling.backend.notification.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReminderNotificationContextDto {
    private Integer reminderId;
    private String summaryTitle;
    private Integer userId;
    private String nickname;
    private String fcmToken;
}
