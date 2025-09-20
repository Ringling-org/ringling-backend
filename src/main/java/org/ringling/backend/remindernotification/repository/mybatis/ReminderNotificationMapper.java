package org.ringling.backend.remindernotification.repository.mybatis;

import froggy.mybatis.winter.annotation.Mapper;
import org.ringling.backend.remindernotification.entity.ReminderNotification;

@Mapper
public interface ReminderNotificationMapper {
    int save(ReminderNotification reminderNotification);
    int merge(ReminderNotification reminderNotification);
    ReminderNotification findById(Integer id);
}
