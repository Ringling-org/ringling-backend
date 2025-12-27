package org.ringling.backend.auth.controller;

import static org.ringling.backend.common.code.ErrorCode.INVALID_NICKNAME;
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
import java.io.IOException;
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

    @RequestMapping(value = "/login/kakao", method = { RequestMethod.POST })
    @ResponseBody
    public ApiResponse<?> kakaoLogin(
        @RequestParam("code") String code, HttpServletResponse response
    ) {
        try {
            AuthToken authToken = authService.processLogin(code);
            addRefreshTokenCookie(response, authToken.getRefreshToken());

            return ApiResponse.success(authToken.getAccessToken());
        } catch (IOException e) {
            log.warn("IOException during Kakao API communication", e);
            return ApiResponse.error(UNEXPECTED_ERROR);
        }
    }

    @RequestMapping(value = "/logout/kakao", method = { RequestMethod.POST })
    @ResponseBody
    public ApiResponse<?> logout(
        @JwtAuth User user,
        HttpServletResponse response
    ) {
        authService.processLogout(user.getId());
        addExpiredRefreshTokenCookie(response);

        return ApiResponse.success(null);
    }

    @RequestMapping(value = "/signup/kakao", method = { RequestMethod.POST })
    @ResponseBody
    public ApiResponse<?> kakaoSignup(
        @RequestParam("code") String code,
        @RequestParam("nickname") String nickname
    ) {
        validateNickname(nickname);

        authService.processSignUp(code, nickname);

        return ApiResponse.success(null);
    }

    @RequestMapping(value = "/refresh", method = { RequestMethod.POST })
    @ResponseBody
    public ApiResponse<?> silentRefresh(@CookieValue(value = "refreshToken", required = false) String refreshToken) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new AuthException(REFRESH_TOKEN_NOT_FOUND);
        }

        String accessToken = authService.silentRefresh(refreshToken);
        return ApiResponse.success(accessToken);
    }

    private void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        cookieUtils.set(REFRESH_TOKEN, refreshToken)
            .path("/api")
            .maxAge(refreshTokenExpirationMs / 1000)
            .sameSite("Strict")
            .build(response);
    }

    private void addExpiredRefreshTokenCookie(HttpServletResponse response) {
        cookieUtils.delete(REFRESH_TOKEN)
            .path("/api")
            .build(response);
    }

    private void validateNickname(String nickname) {
        int nickNameMaxLength = 15;
        if (nickname == null || nickname.trim().isEmpty() || nickname.length() > nickNameMaxLength) {
            throw new AuthException(INVALID_NICKNAME);
        }
    }
}
