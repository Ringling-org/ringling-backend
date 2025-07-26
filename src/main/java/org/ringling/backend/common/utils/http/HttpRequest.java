package org.ringling.backend.common.utils.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

public class HttpRequest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final HttpURLConnection conn;
    private ContentType contentType;

    public HttpRequest(String url, HttpMethod method) throws IOException {
        URL targetUrl = new URL(url);
        conn = (HttpURLConnection) targetUrl.openConnection();
        conn.setRequestMethod(method.name());
    }

    public HttpRequest contentType(ContentType contentType) {
        if (contentType == null) {
            return this;
        }

        this.contentType = contentType;
        conn.setRequestProperty("Content-Type", contentType.getValue());
        return this;
    }

    public HttpRequest headers(Map<String, String> headers) {
        if (headers == null || headers.isEmpty()) {
            return this;
        }

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            conn.setRequestProperty(entry.getKey(), entry.getValue());
        }
        return this;
    }

    public HttpRequest body(Object requestBody) throws IOException {
        if (requestBody == null) return this;

        conn.setDoOutput(true);

        byte[] payload = serialize(requestBody);
        conn.setFixedLengthStreamingMode(payload.length);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(payload);
        }
        return this;
    }

    public HttpURLConnection build() {
        return conn;
    }

    private byte[] serialize(Object body) throws JsonProcessingException {
        Objects.requireNonNull(contentType, "body() 호출 전에 contentType을 설정해야 합니다.");

        switch (contentType) {
            case FORM_URLENCODED:
                if (!(body instanceof Map))
                    throw new IllegalArgumentException("FORM_URLENCODED 바디는 Map<String, String> 타입이어야 합니다.");
                @SuppressWarnings("unchecked")
                Map<String, String> map = (Map<String, String>) body;
                return toFormUrlEncoded(map);

            case JSON:
                return MAPPER.writeValueAsBytes(body);

            default:
                return body.toString().getBytes(StandardCharsets.UTF_8);
        }
    }

    private byte[] toFormUrlEncoded(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> e : params.entrySet()) {
            if (sb.length() > 0) sb.append('&');
            sb.append(encode(e.getKey()))
                .append('=')
                .append(encode(e.getValue()));
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String encode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

}