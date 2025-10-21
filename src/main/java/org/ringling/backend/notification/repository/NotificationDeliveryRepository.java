package org.ringling.backend.notification.repository;

import java.util.Collection;
import java.util.List;
import org.ringling.backend.notification.dto.ReminderNotificationContextDto;
import org.ringling.backend.notification.entity.NotificationDelivery;

public interface NotificationDeliveryRepository {
    NotificationDelivery save(NotificationDelivery notification);
    NotificationDelivery findById(Integer id);
    List<ReminderNotificationContextDto> findReminderNotificationContext(Collection<Integer> reminderIds);
    List<NotificationDelivery> saveAll(Iterable<NotificationDelivery> entities);
}
