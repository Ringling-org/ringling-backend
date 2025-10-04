package org.ringling.backend.fcm.service;

import static org.ringling.backend.common.code.ErrorCode.FIREBASE_SERVICE_ERROR;
import static org.ringling.backend.common.code.ErrorCode.INVALID_FCM_TOKEN;
import static org.ringling.backend.common.code.ErrorCode.UNREGISTERED_FCM_TOKEN;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import froggy.winterframework.beans.factory.annotation.Autowired;
import froggy.winterframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import org.ringling.backend.fcm.exception.FirebaseException;
import org.ringling.backend.user.entity.User;
import org.ringling.backend.user.service.UserService;

@Slf4j
@Service
public class FcmService {

    private UserService userService;

    @Autowired
    public FcmService(UserService userService) {
        this.userService = userService;
    }

    public void refreshFcmToken(User user, String token) {
        validateFcmToken(token);
        userService.updateFcmToken(user, token);
    }

    private void validateFcmToken(String token) {
        try {
            log.debug("[FCM] Validating token: {}", token);
            Message message = Message.builder()
                .setToken(token)
                .build();

            boolean isDryRun = true;
            FirebaseMessaging.getInstance().send(message, isDryRun);
            log.debug("[FCM] Token validation success ✅");
        } catch (FirebaseMessagingException e) {
            MessagingErrorCode errorCode = e.getMessagingErrorCode();
            log.error("[FCM] token validation failed. ErrorCode: {}, Message: {}, Token: {}",
                errorCode, e.getMessage(), token);

            if (errorCode == MessagingErrorCode.INVALID_ARGUMENT) {
                throw new FirebaseException(INVALID_FCM_TOKEN);
            }

            if (errorCode == MessagingErrorCode.UNREGISTERED) {
                throw new FirebaseException(UNREGISTERED_FCM_TOKEN);
            }

            throw new FirebaseException(FIREBASE_SERVICE_ERROR);
        }
    }
}