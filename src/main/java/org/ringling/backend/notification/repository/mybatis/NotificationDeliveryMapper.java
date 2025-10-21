package org.ringling.backend.notification.repository.mybatis;

import froggy.mybatis.winter.annotation.Mapper;
import java.util.Collection;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.ringling.backend.notification.dto.ReminderNotificationContextDto;
import org.ringling.backend.notification.entity.NotificationDelivery;

@Mapper
public interface NotificationDeliveryMapper {
    int save(NotificationDelivery delivery);
    int saveAll(@Param("deliveries") Iterable<NotificationDelivery> deliveries);
    int merge(NotificationDelivery delivery);
    NotificationDelivery findById(Integer id);
    List<ReminderNotificationContextDto> findReminderNotificationContext(@Param("referenceIds") Collection<Integer> reminderIds);
}
