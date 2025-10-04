package org.ringling.backend.fcm.controller;

import froggy.winterframework.beans.factory.annotation.Autowired;
import froggy.winterframework.stereotype.Controller;
import froggy.winterframework.web.bind.annotation.RequestBody;
import froggy.winterframework.web.bind.annotation.RequestMapping;
import froggy.winterframework.web.bind.annotation.RequestMethod;
import org.ringling.backend.common.code.ErrorCode;
import org.ringling.backend.common.dto.ApiResponse;
import org.ringling.backend.config.JwtAuth;
import org.ringling.backend.fcm.dto.RegisterFcmTokenRequest;
import org.ringling.backend.fcm.exception.FirebaseException;
import org.ringling.backend.fcm.service.FcmService;
import org.ringling.backend.user.entity.User;

@RequestMapping("/api/fcm")
@Controller
public class FcmController {

    private final FcmService fcmService;

    @Autowired
    public FcmController(FcmService fcmService) {
        this.fcmService = fcmService;
    }

    @RequestMapping(value = "fcm-token", method = {RequestMethod.POST})
    public ApiResponse<?> refreshFcmToken(@JwtAuth User user, @RequestBody RegisterFcmTokenRequest fcmToken) {
        if (fcmToken.getToken() == null || fcmToken.getToken().isEmpty()) {
            throw new FirebaseException(ErrorCode.INVALID_FCM_TOKEN);
        }

        fcmService.refreshFcmToken(user, fcmToken.getToken());

        return ApiResponse.successWithNoData();
    }
}