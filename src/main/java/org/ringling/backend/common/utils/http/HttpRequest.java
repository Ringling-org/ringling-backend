package org.ringling.backend.common.utils.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HttpRequest {

    private final HttpURLConnection conn;

    public HttpRequest(String url, HttpMethod method) throws IOException {
        URL targetUrl = new URL(url);
        conn = (HttpURLConnection) targetUrl.openConnection();
        conn.setRequestMethod(method.name());
    }

    public HttpRequest contentType(ContentType contentType) {
        if (contentType == null) {
            return this;
        }

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
        if (requestBody == null) {
            return this;
        }

        conn.setDoOutput(true);
        try (OutputStream os = conn.getOutputStream()) {
            String json = requestBody instanceof String
                ? (String) requestBody
                : new ObjectMapper().writeValueAsString(requestBody);
            os.write(json.getBytes(StandardCharsets.UTF_8));
        }
        return this;
    }

    public HttpURLConnection build() {
        return conn;
    }
}