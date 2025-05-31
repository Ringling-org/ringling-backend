package org.ringling.backend.common.utils.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpClient {

    private static final Logger log = LoggerFactory.getLogger(HttpClient.class);

    public static <T> HttpResponse<T> send(
        HttpMethod requestMethod,
        String requestUrl,
        Object requestBody,
        Map<String, String> headers,
        ContentType contentType,
        Class<T> requiredType
    ) throws IOException {
        HttpURLConnection conn = null;
        try {
            conn = new HttpRequest(requestUrl, requestMethod)
                .headers(headers)
                .contentType(contentType)
                .body(requestBody)
                .build();

            conn.setConnectTimeout(1000);

            int statusCode = conn.getResponseCode();

            InputStream responseStream = statusCode < 400
                ? conn.getInputStream()
                : conn.getErrorStream();

            String responseBody;
            try (BufferedReader in = new BufferedReader(new InputStreamReader(responseStream, "UTF-8"))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                responseBody = response.toString();
            }

            /**
             * TODO: statusCode : 500
             * responseBody : Internal Server Error
             * 일경우
             * T parsedBody = new ObjectMapper().readValue(responseBody, requiredType);
             * 가 실행되면 오류를 뱉음
             * json으로변환하려고하기때문에
             * 당연하지
             */

            T parsedBody = new ObjectMapper().readValue(responseBody, requiredType);
            return new HttpResponse<>(statusCode, conn.getResponseMessage(), parsedBody);
        } catch (SocketTimeoutException e) {
            log.error("Connection timed out while trying to reach server:r: {}", requestUrl, e);
            throw e;
        } catch (ConnectException e) {
            log.error("Failed to connect to server: {}", requestUrl, e);
            throw e;
        } catch (JsonProcessingException e) {
            log.error("JSON deserialization failed (check response format): {}", requestUrl, e);
            throw e;
        } catch (IOException e) {
            log.error("General I/O exception occurred: {}", requestUrl, e);
            throw e;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

}
