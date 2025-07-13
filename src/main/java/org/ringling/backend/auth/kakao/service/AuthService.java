package org.ringling.backend.auth.kakao.service;

import froggy.winterframework.beans.factory.annotation.Autowired;
import froggy.winterframework.stereotype.Service;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.ringling.backend.auth.exception.AuthException;
import org.ringling.backend.auth.jwt.JavaJwtProvider;
import org.ringling.backend.auth.kakao.dto.KakaoAccountProfile;
import org.ringling.backend.auth.kakao.dto.KakaoTokenResponse;
import org.ringling.backend.common.code.ErrorCode;

@Service
public class AuthService {

    private final KakaoAuthProvider kakaoAuthProvider;
    private final JavaJwtProvider jwtProvider;

    @Autowired
    public AuthService(KakaoAuthProvider kakaoAuthProvider, JavaJwtProvider jwtProvider) {
        this.kakaoAuthProvider = kakaoAuthProvider;
        this.jwtProvider = jwtProvider;
    }

    public Map<String, String> processLogin(String code) throws IOException {
        // 1. 인가 코드로 카카오 액세스 토큰 조회
        KakaoTokenResponse kakaoToken = kakaoAuthProvider.getToken(code);
        String kakaoAccessToken = kakaoToken.getAccessToken();

        // 2. 카카오 액세스 토큰으로 사용자 정보 조회
        KakaoAccountProfile profile = kakaoAuthProvider.getKakaoUserId(kakaoAccessToken);
        Long kakaoId = profile.getId();

        Boolean b = false;
        // 3. TODO: DB에서 kakaoId로 사용자 조회 후
        if (b) {
            // 존재 시 userId, 없으면 가입 유도 처리
            throw new AuthException(ErrorCode.SIGNUP_REQUIRED);
        }

        // 임시 조치: 데이터베이스 연동 전까지는 애플리케이션 `userId`를 1로 셋팅
        Integer userId = 1;

        // 4. 애플리케이션의 `userId`를 기반으로 JWT 토큰 생성
        return generateJwtTokens(userId);
    }

    private Map<String, String> generateJwtTokens(Integer userId) {
        String accessToken = jwtProvider.createAccessToken(userId);
        String refreshToken = jwtProvider.createRefreshToken(userId);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);
        return tokens;
    }
}
