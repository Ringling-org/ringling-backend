package org.ringling.backend.auth.kakao.controller;

import froggy.winterframework.beans.factory.annotation.Autowired;
import froggy.winterframework.stereotype.Controller;
import froggy.winterframework.web.bind.annotation.RequestMapping;
import froggy.winterframework.web.bind.annotation.RequestMethod;
import froggy.winterframework.web.bind.annotation.RequestParam;
import froggy.winterframework.web.bind.annotation.ResponseBody;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.ringling.backend.auth.dto.AuthToken;
import org.ringling.backend.auth.kakao.service.AuthService;
import org.ringling.backend.common.code.ErrorCode;
import org.ringling.backend.common.dto.ApiResponse;

@Slf4j
@RequestMapping("/api/auth")
@Controller
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @RequestMapping(value = "/login/kakao", method = {RequestMethod.POST})
    @ResponseBody
    public ApiResponse<?> kakaoLogin(@RequestParam("code") String code) throws IOException {
        AuthToken authToken = authService.processLogin(code);

        if (authToken.getRefreshToken() == null) {
            return ApiResponse.error(ErrorCode.SIGNUP_REQUIRED, authToken);
        }

        return ApiResponse.success(authToken);
    }

    @RequestMapping(value = "/signup/kakao", method = {RequestMethod.POST})
    @ResponseBody
    public ApiResponse<?> kakaoSignup(
        @RequestParam("accessToken") String accessToken,
        @RequestParam("nickname") String nickname) throws IOException {
        return ApiResponse.success(null);
    }
}
