package org.ringling.backend.remindernotification.service;

import static org.ringling.backend.common.code.ErrorCode.ALREADY_EXISTS_REMINDER;
import static org.ringling.backend.common.code.ErrorCode.INVALID_REMINDER_TIME;

import froggy.winterframework.beans.factory.annotation.Autowired;
import froggy.winterframework.stereotype.Service;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.exceptions.PersistenceException;
import org.ringling.backend.remindernotification.dto.RegisterReminderNotificationRequest;
import org.ringling.backend.remindernotification.dto.ReminderNotificationResponse;
import org.ringling.backend.remindernotification.entity.ReminderNotification;
import org.ringling.backend.remindernotification.entity.ReminderNotificationStatus;
import org.ringling.backend.remindernotification.exception.ReminderNotificationException;
import org.ringling.backend.remindernotification.repository.ReminderNotificationRepository;

@Slf4j
@Service
public class ReminderNotificationService {

    private final ReminderNotificationRepository repository;

    @Autowired
    public ReminderNotificationService(ReminderNotificationRepository repository) {
        this.repository = repository;
    }

    /**
     * Request를 기반으로 Reminder 알림 엔티티를 생성 및 저장
     */
    public ReminderNotificationResponse registerNotification(RegisterReminderNotificationRequest notificationRequest, Integer userId) {
        log.info("알림 등록 서비스 시작. userId: {}, snapId: {}", userId, notificationRequest.getSnapId());
        if (notificationRequest.getNotificationTime().isBefore(LocalDateTime.now())) {
            throw new ReminderNotificationException(INVALID_REMINDER_TIME);
        }

        ReminderNotification reminderNotification = ReminderNotification.builder()
            .snapId(notificationRequest.getSnapId())
            .userId(userId)
            .notificationTime(notificationRequest.getNotificationTime())
            .notificationStatus(ReminderNotificationStatus.PENDING)
            .build();

        try {
            ReminderNotification entity = repository.save(reminderNotification);
            log.info("알림 저장 완료. id: {}", entity.getId());
            return new ReminderNotificationResponse(entity);
        } catch (PersistenceException e) {
            Throwable cause = e.getCause();
            if (cause instanceof SQLIntegrityConstraintViolationException) {
                throw new ReminderNotificationException(ALREADY_EXISTS_REMINDER);
            }

            throw e;
        }
    }
}
