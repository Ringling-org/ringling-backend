package org.ringling.backend.auth.kakao.controller;

import froggy.winterframework.beans.factory.annotation.Autowired;
import froggy.winterframework.stereotype.Controller;
import froggy.winterframework.web.bind.annotation.RequestMapping;
import froggy.winterframework.web.bind.annotation.RequestMethod;
import froggy.winterframework.web.bind.annotation.RequestParam;
import froggy.winterframework.web.bind.annotation.ResponseBody;
import java.io.IOException;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.ringling.backend.auth.exception.AuthException;
import org.ringling.backend.auth.kakao.service.AuthService;
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
            Map<String, String> tokens = authService.processLogin(code);
            return ApiResponse.success(tokens);
        } catch (AuthException e) {
            return ApiResponse.error(e.getErrorCode());
        }
    }
}
