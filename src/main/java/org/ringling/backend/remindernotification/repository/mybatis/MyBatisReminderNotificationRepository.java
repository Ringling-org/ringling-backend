package org.ringling.backend.remindernotification.repository.mybatis;

import froggy.winterframework.beans.factory.annotation.Autowired;
import froggy.winterframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import org.ringling.backend.remindernotification.entity.ReminderNotification;
import org.ringling.backend.remindernotification.entity.ReminderNotification.ReminderNotificationStatus;
import org.ringling.backend.remindernotification.repository.ReminderNotificationRepository;

@Repository
public class MyBatisReminderNotificationRepository implements ReminderNotificationRepository {

    private final ReminderNotificationMapper reminderNotificationMapper;

    @Autowired
    public MyBatisReminderNotificationRepository(ReminderNotificationMapper reminderNotificationMapper) {
        this.reminderNotificationMapper = reminderNotificationMapper;
    }

    @Override
    public ReminderNotification save(ReminderNotification reminderNotification) {
        if (reminderNotification.getId() != null) {
            return merge(reminderNotification);
        }
        reminderNotification.prePersist();
        reminderNotificationMapper.save(reminderNotification);

        return reminderNotification;
    }

    private ReminderNotification merge(ReminderNotification reminderNotification) {
        reminderNotification.preUpdate();
        reminderNotificationMapper.merge(reminderNotification);

        return findById(reminderNotification.getId());
    }


    @Override
    public ReminderNotification findById(Integer id) {
        return reminderNotificationMapper.findById(id);
    }

    @Override
    public int updateStatuses(List<Integer> ids, ReminderNotificationStatus status) {
        return reminderNotificationMapper.updateStatuses(ids, status);
    }

    @Override
    public List<Integer> findUnsentNotifications(LocalDateTime start, LocalDateTime end) {
        return reminderNotificationMapper.findUnsentNotifications(start, end);
    }

}
