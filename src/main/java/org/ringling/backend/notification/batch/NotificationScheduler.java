package org.ringling.backend.notification.batch;

import froggy.winterframework.beans.factory.annotation.Autowired;
import froggy.winterframework.beans.factory.annotation.Value;
import froggy.winterframework.stereotype.Component;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.ringling.backend.notification.service.NotificationDeliveryService;

@Slf4j
@Component
public class NotificationScheduler {

    private final ScheduledExecutorService scheduler;
    private final NotificationDeliveryService notificationDeliveryService;
    private final long periodSeconds;

    @Autowired
    public NotificationScheduler(
        NotificationDeliveryService notificationDeliveryService,
        @Value("notification.scheduler.period-seconds") long periodSeconds
    ) {
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.notificationDeliveryService = notificationDeliveryService;
        this.periodSeconds = periodSeconds;

        startScheduler();
    }

    private void startScheduler() {
        long st = System.currentTimeMillis();
        scheduler.scheduleWithFixedDelay(() -> {
            try {
                notificationDeliveryService.processReminderNotifications();
            } catch (Throwable t) {
                log.error("[Notify] FCM 알림 스케줄러 처리중 오류 발생.", t);
            } finally {
                long ms = System.currentTimeMillis() - st;
                log.debug("[Notify] tick 종료 ({} ms)", ms);
            }
        }, 0, periodSeconds, TimeUnit.SECONDS);
    }
}