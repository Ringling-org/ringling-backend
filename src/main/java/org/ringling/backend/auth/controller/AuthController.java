package org.ringling.backend.auth.controller;

import static org.ringling.backend.common.code.ErrorCode.REFRESH_TOKEN_NOT_FOUND;
import static org.ringling.backend.common.code.ErrorCode.UNEXPECTED_ERROR;

import froggy.winterframework.beans.factory.annotation.Autowired;
import froggy.winterframework.beans.factory.annotation.Value;
import froggy.winterframework.stereotype.Controller;
import froggy.winterframework.web.bind.annotation.CookieValue;
import froggy.winterframework.web.bind.annotation.RequestMapping;
import froggy.winterframework.web.bind.annotation.RequestMethod;
import froggy.winterframework.web.bind.annotation.RequestParam;
import froggy.winterframework.web.bind.annotation.ResponseBody;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.ringling.backend.auth.dto.AuthToken;
import org.ringling.backend.auth.exception.AuthException;
import org.ringling.backend.auth.service.AuthService;
import org.ringling.backend.common.dto.ApiResponse;
import org.ringling.backend.config.CookieUtils;
import org.ringling.backend.config.JwtAuth;
import org.ringling.backend.user.entity.User;

@Slf4j
@RequestMapping("/api/auth")
@Controller
public class AuthController {

    private final AuthService authService;
    private final CookieUtils cookieUtils;
    private final int refreshTokenExpirationMs;
    private final String REFRESH_TOKEN = "refreshToken";

    @Autowired
    public AuthController(
        AuthService authService,
        CookieUtils cookieUtils,
        @Value("cookie.refresh-token-expiration-ms") int refreshTokenExpirationMs
    ) {
        this.authService = authService;
        this.cookieUtils = cookieUtils;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    @RequestMapping(value = "/login/kakao", method = {RequestMethod.POST})
    @ResponseBody
    public ApiResponse<?> kakaoLogin(
        @RequestParam("code") String code, HttpServletResponse response
    ) {
        try {
            AuthToken authToken = authService.processLogin(code);
            Cookie cookie = buildRefreshTokenCookie(authToken.getRefreshToken());
            response.addCookie(cookie);

            return ApiResponse.success(authToken.getAccessToken());
        } catch (Exception e) {
            log.warn(e.getMessage(), e.getCause());
            return ApiResponse.error(UNEXPECTED_ERROR);
        }
    }

    @RequestMapping(value = "/logout/kakao", method = {RequestMethod.POST})
    @ResponseBody
    public ApiResponse<?> logout(
        @JwtAuth User user,
        HttpServletResponse response
    ) {
        authService.processLogout(user.getId());
        Cookie cookie = buildExpiredRefreshTokenCookie();
        response.addCookie(cookie);

        return ApiResponse.success(null);
    }

    @RequestMapping(value = "/signup/kakao", method = {RequestMethod.POST})
    @ResponseBody
    public ApiResponse<?> kakaoSignup(
        @RequestParam("code") String code,
        @RequestParam("nickname") String nickname) {
        authService.processSignUp(code, nickname);

        return ApiResponse.success(null);
    }

    @RequestMapping(value = "/refresh", method = {RequestMethod.POST})
    @ResponseBody
    public ApiResponse<?> silentRefresh(@CookieValue(value = "refreshToken", required = false) String refreshToken){
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new AuthException(REFRESH_TOKEN_NOT_FOUND);
        }

        String accessToken = authService.silentRefresh(refreshToken);
        return ApiResponse.success(accessToken);
    }


    private Cookie buildRefreshTokenCookie(String refreshToken) {
        return cookieUtils.buildCookie(REFRESH_TOKEN, refreshToken, refreshTokenExpirationMs/1000);
    }

    private Cookie buildExpiredRefreshTokenCookie() {
        return cookieUtils.buildExpiredCookie(REFRESH_TOKEN);
    }

}
