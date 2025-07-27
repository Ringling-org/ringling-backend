package org.ringling.backend.auth.controller;

import froggy.winterframework.beans.factory.annotation.Autowired;
import froggy.winterframework.stereotype.Controller;
import froggy.winterframework.web.bind.annotation.RequestMapping;
import froggy.winterframework.web.bind.annotation.RequestMethod;
import froggy.winterframework.web.bind.annotation.RequestParam;
import froggy.winterframework.web.bind.annotation.ResponseBody;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.ringling.backend.auth.dto.AuthToken;
import org.ringling.backend.auth.exception.AuthException;
import org.ringling.backend.auth.service.AuthService;
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
        try {
            AuthToken authToken = authService.processLogin(code);
            return ApiResponse.success(authToken);
        } catch (AuthException e) {
            log.error("로그인실패 등록되지않는 사용자입니다", e);
            return ApiResponse.error(e.getErrorCode());
        }
    }

    @RequestMapping(value = "/logout/kakao", method = {RequestMethod.POST})
    @ResponseBody
    public ApiResponse<?> logout(@RequestParam("accessToken") String accessToken) throws IOException {
        Boolean isLoggedOut = authService.processLogout(accessToken);

        return isLoggedOut ?
            ApiResponse.success(null) :
            ApiResponse.error(ErrorCode.LOGOUT_ERROR);
    }


    @RequestMapping(value = "/signup/kakao", method = {RequestMethod.POST})
    @ResponseBody
    public ApiResponse<?> kakaoSignup(
        @RequestParam("code") String code,
        @RequestParam("nickname") String nickname) throws IOException {
        authService.processSignUp(code, nickname);

        return ApiResponse.success(null);
    }
}
