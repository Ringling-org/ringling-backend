package org.ringling.backend.notification.repository.mybatis;

import froggy.winterframework.beans.factory.annotation.Autowired;
import froggy.winterframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.ringling.backend.notification.dto.ReminderNotificationContextDto;
import org.ringling.backend.notification.entity.NotificationDelivery;
import org.ringling.backend.notification.repository.NotificationDeliveryRepository;

@Slf4j
@Repository
public class MyBatisNotificationDeliveryRepository implements NotificationDeliveryRepository {

    private final NotificationDeliveryMapper notificationDeliveryMapper;

    @Autowired
    public MyBatisNotificationDeliveryRepository(
        NotificationDeliveryMapper notificationDeliveryMapper) {
        this.notificationDeliveryMapper = notificationDeliveryMapper;
    }

    @Override
    public NotificationDelivery save(NotificationDelivery delivery) {
        if (delivery.getId() != null) {
            return merge(delivery);
        }
        delivery.prePersist();
        notificationDeliveryMapper.save(delivery);

        return delivery;
    }

    private NotificationDelivery merge(NotificationDelivery delivery) {
        delivery.preUpdate();
        notificationDeliveryMapper.merge(delivery);

        return findById(delivery.getId());
    }

    @Override
    public NotificationDelivery findById(Integer id) {
        return notificationDeliveryMapper.findById(id);
    }

    @Override
    public List<ReminderNotificationContextDto> findReminderNotificationContext(Collection<Integer> reminderIds) {
        return notificationDeliveryMapper.findReminderNotificationContext(reminderIds);
    }

    @Override
    public List<NotificationDelivery> saveAll(Iterable<NotificationDelivery> deliveries) {
        List<NotificationDelivery> deliveryList = new ArrayList<>();
        deliveries.forEach(deliveryList::add);

        if (deliveryList.isEmpty()) {
            return deliveryList;
        }

        for (NotificationDelivery delivery : deliveryList) {
            delivery.prePersist();
        }
        notificationDeliveryMapper.saveAll(deliveryList);
        log.debug("[NotifyRepo] saveAll 완료 - {}건", deliveryList.size());
        return deliveryList;
    }
}
