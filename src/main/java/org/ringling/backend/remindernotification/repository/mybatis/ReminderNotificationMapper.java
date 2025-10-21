package org.ringling.backend.remindernotification.repository.mybatis;

import froggy.mybatis.winter.annotation.Mapper;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.ringling.backend.remindernotification.entity.ReminderNotification;
import org.ringling.backend.remindernotification.entity.ReminderNotification.ReminderNotificationStatus;

@Mapper
public interface ReminderNotificationMapper {
    int save(ReminderNotification reminderNotification);
    int merge(ReminderNotification reminderNotification);
    ReminderNotification findById(Integer id);
    List<Integer> findUnsentNotifications(
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );
    int updateStatuses(
        @Param("ids") List<Integer> ids,
        @Param("status") ReminderNotificationStatus status
    );
}
