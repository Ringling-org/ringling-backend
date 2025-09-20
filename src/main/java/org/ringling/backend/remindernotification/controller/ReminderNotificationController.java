package org.ringling.backend.remindernotification.controller;

import froggy.winterframework.beans.factory.annotation.Autowired;
import froggy.winterframework.stereotype.Controller;
import froggy.winterframework.web.bind.annotation.RequestBody;
import froggy.winterframework.web.bind.annotation.RequestMapping;
import froggy.winterframework.web.bind.annotation.RequestMethod;
import froggy.winterframework.web.bind.annotation.ResponseBody;
import lombok.extern.slf4j.Slf4j;
import org.ringling.backend.common.dto.ApiResponse;
import org.ringling.backend.config.JwtAuth;
import org.ringling.backend.remindernotification.dto.RegisterReminderNotificationRequest;
import org.ringling.backend.remindernotification.dto.ReminderNotificationResponse;
import org.ringling.backend.remindernotification.service.ReminderNotificationService;
import org.ringling.backend.user.entity.User;

@Slf4j
@RequestMapping("/api/reminder-notification")
@Controller
public class ReminderNotificationController {

    private final ReminderNotificationService reminderNotificationService;

    @Autowired
    public ReminderNotificationController(ReminderNotificationService reminderNotificationService) {
        this.reminderNotificationService = reminderNotificationService;
    }

    /**
     * 신규 Reminder 알림 등록
     */
    @RequestMapping(method = {RequestMethod.POST})
    @ResponseBody
    public ApiResponse<ReminderNotificationResponse> createNotification(@JwtAuth User user, @RequestBody RegisterReminderNotificationRequest notificationRequest) {
        log.info("리마인더 알림 등록 요청. userId: {}, snapId: {}, notificationTime: {}",
            user.getId(), notificationRequest.getSnapId(), notificationRequest.getNotificationTime());
        ReminderNotificationResponse result = reminderNotificationService.registerNotification(
            notificationRequest, user.getId());

        log.info("리마인더 알림 등록 완료. notificationId: {}", result.getId());
        return ApiResponse.success(result);
    }
}
