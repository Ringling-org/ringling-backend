package org.ringling.backend.remindernotification.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.ringling.backend.remindernotification.entity.ReminderNotification;
import org.ringling.backend.remindernotification.entity.ReminderNotification.ReminderNotificationStatus;

public interface ReminderNotificationRepository {
    ReminderNotification save(ReminderNotification reminderNotification);
    ReminderNotification findById(Integer id);
    List<Integer> findUnsentNotifications(LocalDateTime start, LocalDateTime end);
    int updateStatuses(List<Integer> ids, ReminderNotificationStatus status);
}
