package org.ringling.backend.snap.dto;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;
import lombok.Getter;

@Getter
public class SnapRequestUrl {

    private static final Pattern ENCODED_PATTERN = Pattern.compile("%[0-9a-fA-F]{2}");

    private final String url;

    public SnapRequestUrl(String url) {
        this.url = normalize(url);
    }

    private String normalize(String rawValue) {
        if (rawValue == null) {
            return null;
        }

        String trimmedValue = rawValue.trim();
        if (trimmedValue.isEmpty()) {
            return trimmedValue;
        }

        return doDecode(trimmedValue);
    }

    private String doDecode(String rawValue) {
        // 디코드 패턴이 없으면 그대로 반환 e.g) "%25" ...
        if (!ENCODED_PATTERN.matcher(rawValue).find()) {
            return rawValue;
        }

        try {
            return URLDecoder.decode(rawValue, StandardCharsets.UTF_8.name());

        } catch (IllegalArgumentException e) {
            return rawValue; // 이미 디코딩된 상태이므로 원본 반환

        } catch (UnsupportedEncodingException e) { // UTF-8은 항상 지원되지만, 메서드 시그니처상 필요한 예외 처리
            throw new IllegalStateException(
                String.format("Unable to decode %s: Standard UTF-8 charset is not available.",
                    this.getClass().getSimpleName()),
                e
            );
        }
    }

    @Override
    public String toString() {
        return "SnapRequestUrl{url='" + url + "'}";
    }
}