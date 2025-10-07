package org.ringling.backend.remindernotification.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.ringling.backend.remindernotification.entity.ReminderNotification;
import org.ringling.backend.remindernotification.entity.ReminderNotification.ReminderNotificationStatus;

@Getter
@AllArgsConstructor
public class ReminderNotificationResponse {

    private final Integer id;
    private final Integer snapId;
    private final LocalDateTime notificationTime;
    private final ReminderNotificationStatus notificationStatus;

    public ReminderNotificationResponse(ReminderNotification entity) {
        this.id = entity.getId();
        this.snapId = entity.getSnapId();
        this.notificationTime = entity.getNotificationTime();
        this.notificationStatus = entity.getNotificationStatus();
    }
}