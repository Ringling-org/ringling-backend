package org.ringling.backend.auth.dto;

import lombok.Getter;

@Getter
public class LoginResponse {
    private final String accessToken;
    private final String refreshToken;

    private LoginResponse(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public static LoginResponse from(AuthToken authToken) {
        return new LoginResponse(authToken.getAccessToken(), authToken.getRefreshToken());
    }
}