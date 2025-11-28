package org.ringling.backend.snap.dto;

import static org.ringling.backend.common.code.ErrorCode.INVALID_SNAP_URL;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.ringling.backend.snap.exception.SnapException;

@Slf4j
@Getter
public class SnapRequestUrl {

    private final String url;

    /**
     * 입력 URL을 디코딩 후 검증
     * (검증 단계에서 보안/형식 오류를 SnapException 으로 throw)
     */
    @JsonCreator
    public SnapRequestUrl(@JsonProperty("url") String url) {
        try {
            // URL 디코딩
            String decodeUrl = doDecode(url);
            // 검증 (잘못된 구조·내부망 접근 등에서 예외 발생)
            this.url = validate(decodeUrl);
        }

        catch (SecurityException e) {
            log.error("[보안 차단] 허용되지 않는 네트워크 접근입니다.", e);
            throw new SnapException(INVALID_SNAP_URL);
        }
        catch (IllegalArgumentException | IllegalStateException e) {
            log.error("[요청 실패] 유효하지 않은 URL입니다.", e);
            throw new SnapException(INVALID_SNAP_URL);
        }
        catch (Exception e) {
            log.error("[시스템 오류] URL 처리 중 문제가 발생했습니다.", e);
            throw new SnapException(INVALID_SNAP_URL);
        }
    }

    private static final Pattern ENCODED_PATTERN = Pattern.compile("%[0-9a-fA-F]{2}");
    private String doDecode(String rawValue) {
        if (rawValue == null) {
            return rawValue;
        }

        rawValue = rawValue.trim();
        if (rawValue.isEmpty()) {
            return rawValue;
        }

        // 디코딩 패턴이 없으면 그대로 반환 e.g) "%25" ...
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

    private String validate(String decodedUrl) {
        if (decodedUrl == null || decodedUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("URL cannot be empty.");
        }
        String urlForParsing = extractBaseUrl(decodedUrl);

        URI uri;
        try {
            uri = new URI(urlForParsing).normalize();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("URL syntax Error: " + decodedUrl, e); // URL Syntax 에러
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to create URI instance: " + decodedUrl, e);
        }

        String scheme = uri.getScheme();
        if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
            throw new IllegalArgumentException("Only HTTP/HTTPS protocols are allowed");  // Unsupport Scheme
        }

        // 비정상 URL을 host=null로 파싱하는 경우가 있어 따로 검사
        //  - http:/host.com
        //  - http:/127.1
        String host = uri.getHost();
        if (host == null) {
            throw new IllegalArgumentException("Invalid URL format: Host not found in [" + urlForParsing + "]");
        }

        // DNS 조회 - rseolved된 IP를 찾아 SSRF 검증
        InetAddress addr;
        try {
            addr = InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Domain not found (DNS lookup failed): " + host, e);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error during DNS resolution: " + host, e);
        }

        String ip = addr.getHostAddress();

        // Private Network, Local Network Filter
        if (isUnsafeIp(addr, ip)) {
            throw new SecurityException("SSRF Blocked: Access to internal network denied (" + host + " / " + ip + ")");
        }

        // 모든 검증통과 - 디코딩된 URL 전달
        return decodedUrl;
    }

    /**
     * URI 파싱 전에 Query(?)·Fragment(#)를 제거하여 기본 URL만 추출.
     * 예) "http://test.com/path?q=100%" → "http://test.com/path"
     * (% 등 미인코딩 문자가 쿼리에 있을 경우 URI 생성 오류 방지용)
     */
    private String extractBaseUrl(String url) {
        int queryIdx = url.indexOf('?');
        int fragmentIdx = url.indexOf('#');

        // 둘 다 없으면 원본 그대로
        if (queryIdx == -1 && fragmentIdx == -1) {
            return url;
        }

        // 둘 중 더 앞에 있는 기호 기준으로 자름
        int cutIndex;
        if (queryIdx != -1 && fragmentIdx != -1) {
            cutIndex = Math.min(queryIdx, fragmentIdx);
        } else {
            cutIndex = (queryIdx != -1) ? queryIdx : fragmentIdx;
        }

        return url.substring(0, cutIndex);
    }

    /**
     * SSRF Filter 내부망·로컬망·메타데이터 IP 등 위험한 대상인지 검사
     */
    private boolean isUnsafeIp(InetAddress addr, String ip) {
        // 127.0.0.1, 0.0.0.0, ::1
        if (addr.isLoopbackAddress() || ip.equals("127.0.0.1") || ip.equals("0.0.0.0")) {
            return true;
        }
        // 192.168.x.x, 10.x.x.x, 172.16.x.x
        if (addr.isSiteLocalAddress()) {
            return true;
        }
        // 링크 로컬 / 클라우드 메타데이터 169.254.x.x
        if (addr.isLinkLocalAddress() || ip.startsWith("169.254")) {
            return true;
        }
        // 224.0.0.0 ~
        if (addr.isMulticastAddress()) {
            return true;
        }

        return false;
    }

    @Override
    public String toString() {
        return "SnapRequestUrl{url='" + url + "'}";
    }
}