package org.ringling.backend.common.utils;

import java.nio.charset.StandardCharsets;

public class StringUtils {

    /**
     * 문자열을 UTF-8 인코딩 기준 최대 바이트 길이를 넘지 않도록 안전하게 자릅니다.
     * 잘린 문자열의 끝에는 ...이 추가됩니다.
     *
     * @param text     원본 문자열
     * @param maxBytes 최대 허용 바이트 (예: DB 컬럼 크기)
     * @return 바이트 길이에 맞춰 잘린 문자열
     */
    public static String truncateByBytes(String text, int maxBytes) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);

        if (bytes.length <= maxBytes) {
            return text;
        }

        int validLength = maxBytes - 3;
        while (validLength > 0 && (bytes[validLength] & 0xC0) == 0x80) {
            validLength--;
        }

        // 유효한 길이로 문자열을 생성하고 ...를 붙임
        return new String(bytes, 0, validLength, StandardCharsets.UTF_8) + "...";
    }
}
