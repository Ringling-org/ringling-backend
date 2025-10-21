package org.ringling.backend.notification.entity;

import static org.ringling.backend.notification.entity.NotificationDelivery.Result.FAILED;
import static org.ringling.backend.notification.entity.NotificationDelivery.Result.SUCCESS;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MessagingErrorCode;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.ringling.backend.common.entity.BaseEntity;

@SuperBuilder
@Getter
@NoArgsConstructor
@JsonPropertyOrder({
    "id",
    "referenceId",
    "attemptedAt",
    "result",
    "errorCode",
    "reason",
    "createdAt",
    "updatedAt"
})
public class NotificationDelivery extends BaseEntity {

    private Integer id;
    private Integer referenceId;
    private LocalDateTime attemptedAt;
    private Result result;
    private ErrorCode errorCode;
    private String reason;

    public enum Result {
        SUCCESS,
        FAILED
    }

    @Getter
    public enum ErrorCode {
        INVALID_ARGUMENT("잘못된 요청 인수"),
        INTERNAL("FCM 서버 내부 오류"),
        QUOTA_EXCEEDED("FCM 할당량 초과"),
        SENDER_ID_MISMATCH("FCM Sender ID 불일치 (서버 설정 오류)"),
        UNAVAILABLE("FCM 서버 일시적 오류"),
        UNREGISTERED("유효하지 않은 FCM Token"),
        UNKNOWN("알 수 없는 오류");

        private final String defaultReason;
        public static final String INVALID_TOKEN_MESSAGE = "The registration token is not a valid FCM registration token";

        ErrorCode(String defaultReason) {
            this.defaultReason = defaultReason;
        }

        String getReason() {
            return defaultReason;
        }


        public static ErrorCode from(FirebaseMessagingException e) {
            MessagingErrorCode errorCode = e.getMessagingErrorCode();
            if (errorCode == null) {
                return NotificationDelivery.ErrorCode.UNKNOWN;
            }

            switch (errorCode) {
                case INVALID_ARGUMENT:
                    return ErrorCode.INVALID_ARGUMENT;
                case INTERNAL:
                    return ErrorCode.INTERNAL;
                case QUOTA_EXCEEDED:
                    return ErrorCode.QUOTA_EXCEEDED;
                case SENDER_ID_MISMATCH:
                    return ErrorCode.SENDER_ID_MISMATCH;
                case UNAVAILABLE:
                    return ErrorCode.UNAVAILABLE;
                case UNREGISTERED:
                    return ErrorCode.UNREGISTERED;
                default:
                    return ErrorCode.UNKNOWN;
            }
        }
    }

    public static NotificationDelivery successOf(Integer referenceId, LocalDateTime attemptedAt) {
        return NotificationDelivery.builder()
            .referenceId(referenceId)
            .attemptedAt(attemptedAt)
            .result(SUCCESS)
            .build();
    }

    // 명시적인 실패 사유(reason)를 포함한 실패 알림 생성
    public static NotificationDelivery failOf(
        Integer referenceId,
        LocalDateTime attemptedAt,
        ErrorCode errorCode,
        String reason
    ) {
        return NotificationDelivery.builder()
            .referenceId(referenceId)
            .attemptedAt(attemptedAt)
            .result(FAILED)
            .errorCode(errorCode)
            .reason(reason)
            .build();
    }

    // 내부 정의된 ErrorCode의 사유를 설정해 실패 알림 생성
    public static NotificationDelivery failOf(
        Integer referenceId,
        LocalDateTime attemptedAt,
        ErrorCode errorCode
    ) {
        return failOf(referenceId, attemptedAt, errorCode, errorCode.getReason());
    }

}
