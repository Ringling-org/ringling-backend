package org.ringling.backend.config;

import froggy.winterframework.stereotype.Component;
import javax.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@Component
public class CookieUtils {

    public CookieBuilder set(String name, String value) {
        return new CookieBuilder(name, value);
    }

    public CookieBuilder delete(String name) {
        return set(name, "").maxAge(0);
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class CookieBuilder {
        private final String name;
        private final String value;
        private int maxAge = -1;
        private String path = "/";
        private String domain;
        private String sameSite = "Lax";
        private boolean httpOnly = true;
        private boolean secure = true;

        public CookieBuilder maxAge(int maxAge) { this.maxAge = maxAge; return this; }
        public CookieBuilder path(String path) { this.path = path; return this; }
        public CookieBuilder domain(String domain) { this.domain = domain; return this; }
        public CookieBuilder httpOnly(boolean httpOnly) { this.httpOnly = httpOnly; return this; }
        public CookieBuilder secure(boolean secure) { this.secure = secure; return this; }
        public CookieBuilder sameSite(String sameSite) { this.sameSite = sameSite; return this; }

        public CookieBuilder sameSiteLax() {
            this.sameSite = "Lax";
            return this;
        }

        public CookieBuilder sameSiteStrict() {
            this.sameSite = "Strict";
            return this;
        }

        public CookieBuilder sameSiteNone() {
            this.sameSite = "None";
            this.secure = true;
            return this;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();

            sb.append(name).append("=").append(value);

            if (maxAge >= 0) sb.append("; Max-Age=").append(maxAge);
            if (path != null) sb.append("; Path=").append(path);
            if (domain != null) sb.append("; Domain=").append(domain);
            if (httpOnly) sb.append("; HttpOnly");
            if (secure) sb.append("; Secure");
            if (sameSite != null) sb.append("; SameSite=").append(sameSite);

            return sb.toString();
        }

        public void build(HttpServletResponse response) {
            response.addHeader("Set-Cookie", this.toString());
        }
    }
}