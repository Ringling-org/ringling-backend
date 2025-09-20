package org.ringling.backend.remindernotification.entity;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.ringling.backend.common.entity.BaseEntity;

@SuperBuilder
@Getter
@NoArgsConstructor
@JsonPropertyOrder({
    "id",
    "userId",
    "snapId",
    "notificationTime",
    "notificationStatus",
    "createdAt",
    "updatedAt"
})
public class ReminderNotification extends BaseEntity {
    private Integer id;
    private Integer userId;
    private Integer snapId;
    private LocalDateTime notificationTime;
    private ReminderNotificationStatus notificationStatus;
}
