package org.ringling.backend.auth.kakao.service;

import froggy.winterframework.beans.factory.annotation.Autowired;
import froggy.winterframework.beans.factory.annotation.Value;
import froggy.winterframework.stereotype.Service;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.ringling.backend.auth.kakao.dto.KaKaoLogoutInfo;
import org.ringling.backend.auth.kakao.dto.KakaoAccountProfile;
import org.ringling.backend.auth.kakao.dto.KakaoTokenResponse;
import org.ringling.backend.common.utils.http.ContentType;
import org.ringling.backend.common.utils.http.HttpClient;
import org.ringling.backend.common.utils.http.HttpMethod;

@Service
public class KakaoAuthProvider {

    @Autowired
    public KakaoAuthProvider(
        @Value("kakao.client-id") String clientId,
        @Value("kakao.client-secret") String clientSecret,
        @Value("kakao.redirect-uri") String redirectUri,
        @Value("kakao.auth.token-uri") String authTokenUri,
        @Value("kakao.auth.user-info-uri") String authUserInfoUri,

        @Value("kakao.auth.logout-uri") String authLogoutUri
    ) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;

        this.authTokenUri=authTokenUri;
        this.authUserInfoUri=authUserInfoUri;

        this.authLogoutUri=authLogoutUri;
    }
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;

    private final String authTokenUri;
    private final String authUserInfoUri;

    private final String authLogoutUri;

    public KakaoTokenResponse getToken(String code) throws IOException {
        Map<String, String> body = new HashMap<>();
        body.put("grant_type", "authorization_code");
        body.put("client_id", clientId);
        body.put("redirect_uri", redirectUri);
        body.put("code", code);
        body.put("client_secret", clientSecret);

        return HttpClient.send(
            HttpMethod.POST,
            authTokenUri,
            body,
            null,
            ContentType.FORM_URLENCODED,
            KakaoTokenResponse.class
        ).getResponseBody();
    }

    public KakaoAccountProfile getKakaoUserId(String kakaoAccessToken) throws IOException {
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Authorization", "Bearer " + kakaoAccessToken);
        headersMap.put("Content-Type", ContentType.FORM_URLENCODED.name());

        return HttpClient.send(
            HttpMethod.POST,
            authUserInfoUri,
            null,
            headersMap,
            ContentType.FORM_URLENCODED,
            KakaoAccountProfile.class
        ).getResponseBody();
    }

    /**
     * 카카오 로그아웃 요청을 보내고, 성공했으면 true를 리턴합니다.
     */
    public boolean logout(String kakaoAccessToken) throws IOException {
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Authorization", "Bearer " + kakaoAccessToken);
        headersMap.put("Content-Type", ContentType.FORM_URLENCODED.name());

        // 로그아웃 성공시 사용자의 회원번호 반환
        KaKaoLogoutInfo logoutInfo = HttpClient.send(
            HttpMethod.POST,
            authLogoutUri,
            null,
            headersMap,
            ContentType.FORM_URLENCODED,
            KaKaoLogoutInfo.class
        ).getResponseBody();

        return logoutInfo.getId() != null;
    }
}
