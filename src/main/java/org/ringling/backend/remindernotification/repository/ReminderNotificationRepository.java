package org.ringling.backend.remindernotification.repository;

import org.ringling.backend.remindernotification.entity.ReminderNotification;

public interface ReminderNotificationRepository {
    ReminderNotification save(ReminderNotification reminderNotification);
    ReminderNotification findById(Integer id);
}
