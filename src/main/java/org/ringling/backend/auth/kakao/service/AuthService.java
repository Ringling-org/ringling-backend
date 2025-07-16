package org.ringling.backend.auth.kakao.service;

import froggy.winterframework.beans.factory.annotation.Autowired;
import froggy.winterframework.stereotype.Service;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.ringling.backend.auth.dto.AuthToken;
import org.ringling.backend.auth.exception.AuthException;
import org.ringling.backend.auth.jwt.JavaJwtProvider;
import org.ringling.backend.auth.kakao.dto.KakaoAccountProfile;
import org.ringling.backend.auth.kakao.dto.KakaoTokenResponse;
import org.ringling.backend.common.code.ErrorCode;
import org.ringling.backend.user.entity.SocialType;
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
        // 1. 인가 코드로 카카오 액세스 토큰 조회
        KakaoTokenResponse kakaoToken = kakaoAuthProvider.getToken(code);
        String kakaoAccessToken = kakaoToken.getAccessToken();

        // 2. 카카오 액세스 토큰으로 사용자 정보 조회
        KakaoAccountProfile profile = kakaoAuthProvider.getKakaoUserId(kakaoAccessToken);
        Long kakaoId = profile.getId();

        User user = userService.findByKaKaoId(kakaoId);
        if (user == null) {
            return new AuthToken(kakaoAccessToken, null);
        }

        // 4. 애플리케이션의 `userId`를 기반으로 JWT 토큰 생성
        AuthToken response = generateJwtTokens(user.getId());

        user.issueRefreshToken(response.getRefreshToken());
        userService.save(user);
        return response;
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

            User selectUser = userService.findByKaKaoId(kakaoId);
            if (selectUser != null) {
                throw new AuthException(ErrorCode.EXISTS_USER);
            }

            User user = User.builder()
                .socialId(kakaoId)
                .socialType(SocialType.KAKAO)
                .nickname(nickname)
                .build();

            user.prePersist();
            userService.save(user);
            log.info("user가입완료 " + user.getId());
        } catch (Exception e) {
            log.error("User 저장 중 오류 발생", e);
        }
    }
}
