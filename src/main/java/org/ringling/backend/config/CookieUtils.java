package org.ringling.backend.config;

import froggy.winterframework.stereotype.Component;
import javax.servlet.http.Cookie;

@Component
public class CookieUtils {

    /**
     * 기본 보안정책이 적용된 쿠키를 생성한다.
     *
     * @param key   쿠키 이름
     * @param value 쿠키 값
     * @param maxAgeSeconds 만료시간 (초 단위)
     * @return Cookie
     */
    private Cookie doBuildCookie(String key, String value, int maxAgeSeconds) {
        Cookie cookie = new Cookie(key, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(maxAgeSeconds);
        return cookie;
    }

    public Cookie buildCookie(String key, String value, int maxAgeSeconds) {
        return doBuildCookie(key, value, maxAgeSeconds);
    }

    /**
     * 즉시 만료시킬 쿠키 (삭제용)
     */
    public Cookie buildExpiredCookie(String key) {
        return doBuildCookie(key, "", 0);
    }
}
