package org.ringling.backend.auth.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import froggy.winterframework.beans.factory.annotation.Autowired;
import froggy.winterframework.beans.factory.annotation.Value;
import froggy.winterframework.stereotype.Component;
import java.time.Instant;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JavaJwtProvider {

    private final String issuer;
    private final long accessTokenExpirationMs;
    private final long refreshTokenExpirationMs;

    private Algorithm algorithm;
    private JWTVerifier verifier;

    @Autowired
    public JavaJwtProvider(
        @Value("jwt.secret-key") String secretKey,
        @Value("jwt.issuer") String issuer,
        @Value("jwt.access-token-expiration-ms") long accessTokenExpirationMs,
        @Value("jwt.refresh-token-expiration-ms") long refreshTokenExpirationMs
    ) {
        this.issuer = issuer;
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;

        this.algorithm = Algorithm.HMAC256(secretKey);
        this.verifier = JWT.require(algorithm)
            .withIssuer(issuer)
            .build();
    }

    public String createAccessToken(Integer userId) {
        return createAccessToken(userId, accessTokenExpirationMs);
    }

    public String createAccessToken(Integer userId, Long expirationMs) {
        return createJWTToken(userId, TokenType.AccessToken, expirationMs);
    }

    public String createRefreshToken(Integer userId) {
        return createRefreshToken(userId, refreshTokenExpirationMs);
    }

    public String createRefreshToken(Integer userId, Long expirationMs) {
        return createJWTToken(userId, TokenType.RefreshToken, expirationMs);
    }

    /**
     * JWT 토큰을 생성합니다.
     *
     * @param userId     사용자 식별자
     * @param tokenType  토큰 타입 (AccessToken 또는 RefreshToken)
     * @param expirationMs 만료 시간 (ms)
     * @return 서명된 JWT 문자열
     */
    private String createJWTToken(Integer userId, TokenType tokenType, Long expirationMs) {
        Instant now = Instant.now();

        return JWT.create()
            .withIssuer(issuer)
            .withClaim("token_type", tokenType.name())
            .withSubject(userId.toString())
            .withIssuedAt(Date.from(now))
            .withExpiresAt(Date.from(now.plusMillis(expirationMs)))
            .sign(algorithm);
    }

    /**
     * JWT 토큰이 유효한지 검증합니다.
     *
     * @param token JWT 문자열
     * @return 유효하면 true, 아니면 false
     */
    public boolean validateToken(String token) {
        try {
            verifier.verify(token);
            return true;
        } catch (TokenExpiredException ex) {
            log.warn("만료된 토큰");
            return false;
        } catch (JWTVerificationException ex) {
            log.warn("토큰 검증 실패: " + ex.getMessage());
            return false;
        }
    }

    /**
     * JWT 토큰에서 Claims(정보)를 파싱합니다.
     *
     * @param token JWT 문자열
     * @return DecodedJWT 파싱된 토큰 정보
     * @throws JWTVerificationException 검증 실패(만료 등) 시 예외 발생
     */
    public DecodedJWT parseClaims(String token) throws JWTVerificationException {
        return verifier.verify(token);
    }
}
