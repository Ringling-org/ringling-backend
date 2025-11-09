package org.ringling.backend.auth.service;

import static org.ringling.backend.common.code.ErrorCode.REFRESH_TOKEN_INVALID;
import static org.ringling.backend.common.code.ErrorCode.USER_NOT_FOUND;

import com.auth0.jwt.interfaces.DecodedJWT;
import froggy.winterframework.beans.factory.annotation.Autowired;
import froggy.winterframework.stereotype.Service;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.ringling.backend.auth.dto.AuthToken;
import org.ringling.backend.auth.exception.AuthException;
import org.ringling.backend.auth.jwt.JavaJwtProvider;
import org.ringling.backend.auth.kakao.dto.KakaoAccountProfile;
import org.ringling.backend.auth.kakao.dto.KakaoTokenResponse;
import org.ringling.backend.auth.kakao.service.KakaoAuthProvider;
import org.ringling.backend.common.code.ErrorCode;
import org.ringling.backend.user.entity.User;
import org.ringling.backend.user.service.UserService;

@Slf4j
@Service
public class AuthService {

    private final KakaoAuthProvider kakaoAuthProvider;
    private final JavaJwtProvider jwtProvider;
    private final UserService userService;

    @Autowired
    public AuthService(
        KakaoAuthProvider kakaoAuthProvider,
        JavaJwtProvider jwtProvider,
        UserService userService
    ) {
        this.kakaoAuthProvider = kakaoAuthProvider;
        this.jwtProvider = jwtProvider;
        this.userService = userService;
    }

    public AuthToken processLogin(String code) throws IOException {
        KakaoTokenResponse kakaoToken = kakaoAuthProvider.getToken(code);
        String kakaoAccessToken = kakaoToken.getAccessToken();

        KakaoAccountProfile profile = kakaoAuthProvider.getKakaoUserId(kakaoAccessToken);
        Long kakaoId = profile.getId();

        User user = userService.findByKaKaoId(kakaoId);

        kakaoAuthProvider.logout(kakaoAccessToken);
        if (user == null) {
            throw new AuthException(ErrorCode.SIGNUP_REQUIRED);
        }

        AuthToken token = generateJwtTokens(user.getId());
        userService.login(user, token.getRefreshToken());

        return token;
    }

    private AuthToken generateJwtTokens(Integer userId) {
        String accessToken = jwtProvider.createAccessToken(userId);
        String refreshToken = jwtProvider.createRefreshToken(userId);

        return new AuthToken(accessToken, refreshToken);
    }

    public void processSignUp(String code, String nickname) {
        try {
            KakaoTokenResponse kakaoToken = kakaoAuthProvider.getToken(code);
            String kakaoAccessToken = kakaoToken.getAccessToken();

            KakaoAccountProfile profile = kakaoAuthProvider.getKakaoUserId(kakaoAccessToken);
            Long kakaoId = profile.getId();

            User user = userService.signUpFromKakao(kakaoId, nickname);
            log.info("user가입완료 " + user.getId());
        } catch (Exception e) {
            log.error("User 저장 중 오류 발생", e);
        }
    }

    public void processLogout(Integer userId) {
        User user = userService.getUserByUserId(userId);
        if (user == null) {
            throw new AuthException(USER_NOT_FOUND);
        }

        if (user.getRefreshToken() == null) {
            log.warn("이미 로그아웃된 유저입니다. ID: {}", userId);
            return;
        }

        userService.clearRefreshToken(userId);
    }

    public String silentRefresh(String refreshToken) {
        if (!jwtProvider.validateToken(refreshToken)) {
            log.warn("Logout 실패: invalid token={}", refreshToken);
            throw new AuthException(REFRESH_TOKEN_INVALID);
        }

        DecodedJWT decoded = jwtProvider.parseClaims(refreshToken);
        Integer userId = Integer.parseInt(decoded.getSubject());

        User user = userService.getUserByUserId(userId);
        // DB에 저장된 refreshToken과 비교
        String storedRefreshToken = user.getRefreshToken();

        if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
            log.warn(
                "silentRefresh 실패: stored refreshToken 불일치. userId={}, stored={}, request={}",
                userId, storedRefreshToken, refreshToken
            );
            throw new AuthException(REFRESH_TOKEN_INVALID);
        }
        return jwtProvider.createAccessToken(userId);
    }
}
