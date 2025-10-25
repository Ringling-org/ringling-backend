package org.ringling.backend.notification.service;

import static org.ringling.backend.notification.entity.NotificationDelivery.ErrorCode.INVALID_TOKEN_MESSAGE;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.SendResponse;
import froggy.winterframework.beans.factory.annotation.Autowired;
import froggy.winterframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.ringling.backend.common.utils.StringUtils;
import org.ringling.backend.notification.dto.ReminderNotificationContextDto;
import org.ringling.backend.notification.entity.NotificationDelivery;
import org.ringling.backend.notification.entity.NotificationDelivery.ErrorCode;
import org.ringling.backend.notification.repository.NotificationDeliveryRepository;
import org.ringling.backend.remindernotification.entity.ReminderNotification.ReminderNotificationStatus;
import org.ringling.backend.remindernotification.service.ReminderNotificationService;

@Slf4j
@Service
public class NotificationDeliveryService {

    private final ReminderNotificationService reminderNotificationService;
    private final NotificationDeliveryRepository notificationDeliveryRepository;
    private final String reminderTitle = "\uD83D\uDCA1 좋은 아이디어는 읽을 때 떠오르죠!";
    private final int BATCH_SIZE = 500;

    @Autowired
    public NotificationDeliveryService(
        ReminderNotificationService reminderNotificationService,
        NotificationDeliveryRepository notificationDeliveryRepository
    ) {
        this.reminderNotificationService = reminderNotificationService;
        this.notificationDeliveryRepository = notificationDeliveryRepository;
    }

    public void processReminderNotifications() {
        // 1. 알림 대상 ID 찾기
        List<Integer> candidateIds = reminderNotificationService.findUnsentNotification();
        if (candidateIds.isEmpty()) {
            return;
        }

        // 그외 정보들 가져오기 User정보, FCM Token, 게시글정보
        List<ReminderNotificationContextDto> notificationContexts =
            notificationDeliveryRepository.findReminderNotificationContext(candidateIds);

        for (int i = 0; i < notificationContexts.size(); i += BATCH_SIZE) {
            int end = Math.min(notificationContexts.size(), i + BATCH_SIZE);
            List<ReminderNotificationContextDto> batchList = notificationContexts.subList(i, end);
            log.info("[Notify] FCM 배치 시작 - 대상: {}건", batchList.size());

            // 알림 프로세스 시작 전 처리상태 셋팅
            List<Integer> notificationIds = batchList.stream()
                .map(ReminderNotificationContextDto::getReminderId)
                .collect(Collectors.toList());
            
            reminderNotificationService.updateStatusesIfNotEmpty(
                notificationIds,
                ReminderNotificationStatus.PROCESSING
            );

            // FCM 메시지 객체 준비하기 Bulid
            List<Message> messages = buildReminderNotificationMessages(batchList);

            // sendEach() 호출
            try {
                LocalDateTime batchAttemptedAt = LocalDateTime.now();
                BatchResponse response = FirebaseMessaging.getInstance().sendEach(messages);
                log.info("[Notify] FCM 배치 전송 - 성공: {}, 실패: {}, 배치크기: {}",
                    response.getSuccessCount(), response.getFailureCount(), messages.size()
                );

                // 각 Response 핸들링
                handleBatchResponse(response, notificationContexts, batchAttemptedAt);

            } catch (FirebaseMessagingException e) {
                log.error("[Notify] FCM 전송 예외 - code={}, msg={}", e.getMessagingErrorCode(), e.getMessage());
            }
        }
    }

    private List<Message> buildReminderNotificationMessages(
        List<ReminderNotificationContextDto> contexts
    ) {
        return contexts.stream()
            .map(context -> Message.builder()
                .setToken(context.getFcmToken())
                .putData("title", reminderTitle)
                .putData("body", context.getSummaryTitle())
                .build())
            .collect(Collectors.toList());
    }


    private void handleBatchResponse(
        BatchResponse response,
        List<ReminderNotificationContextDto> contexts,
        LocalDateTime batchAttemptedAt
    ) {
        saveDeliveryResults(response, contexts, batchAttemptedAt);
        updateReminderStatuses(response, contexts);
    }

    private void updateReminderStatuses(
        BatchResponse response,
        List<ReminderNotificationContextDto> contexts
    ) {
        List<Integer> sentIds = new ArrayList<>();
        List<Integer> failedIds = new ArrayList<>();

        List<SendResponse> sendResults = response.getResponses();
        for (int i = 0; i < response.getResponses().size(); i++) {
            if (sendResults.get(i).isSuccessful()) {
                sentIds.add(contexts.get(i).getReminderId());
            }
            else {
                failedIds.add(contexts.get(i).getReminderId());
            }
        }

        reminderNotificationService.updateStatusesIfNotEmpty(sentIds, ReminderNotificationStatus.SENT);
        reminderNotificationService.updateStatusesIfNotEmpty(failedIds, ReminderNotificationStatus.FAILED);
    }

    private void saveDeliveryResults(
        BatchResponse response,
        List<ReminderNotificationContextDto> contexts,
        LocalDateTime batchAttemptedAt
    ) {
        List<NotificationDelivery> notificationDeliveries = new ArrayList<>();
        for (int i = 0; i < response.getResponses().size(); i++) {
            SendResponse sendResponse = response.getResponses().get(i);
            Integer reminderId = contexts.get(i).getReminderId();

            if (sendResponse.isSuccessful()) {
                notificationDeliveries.add(
                    NotificationDelivery.successOf(reminderId, batchAttemptedAt)
                );
            }
            else {
                notificationDeliveries.add(
                    buildFailedDelivery(
                        reminderId,
                        batchAttemptedAt,
                        sendResponse.getException()
                    )
                );
            }
        }

        if (notificationDeliveries.isEmpty()) return;

        notificationDeliveryRepository.saveAll(notificationDeliveries);
    }

    private NotificationDelivery buildFailedDelivery(
        Integer reminderId,
        LocalDateTime AttemptedAt,
        Exception exception
    ) {
        if (exception instanceof FirebaseMessagingException) {
            FirebaseMessagingException e = (FirebaseMessagingException) exception;
            ErrorCode errorCode = ErrorCode.from(e);

            if (errorCode == ErrorCode.INVALID_ARGUMENT) {
                String reason = INVALID_TOKEN_MESSAGE.equals(e.getMessage())
                    ? "FCM Token 형식 오류"
                    : "잘못된 메시지 형식";
                return NotificationDelivery.failOf(reminderId, AttemptedAt, errorCode, reason);
            }
            else {
                return NotificationDelivery.failOf(reminderId, AttemptedAt, errorCode);
            }
        } else {
            return NotificationDelivery.failOf(
                reminderId,
                AttemptedAt,
                ErrorCode.UNKNOWN,
                "알 수 없는 내부 오류: " + StringUtils.truncateByBytes(exception.getMessage(), 255)
            );
        }
    }
}
