package org.ringling.backend.config;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ValidSnapUrlValidator implements ConstraintValidator<ValidSnapUrl, String> {

    private static final Pattern ENCODED_PATTERN = Pattern.compile("%[0-9a-fA-F]{2}");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        String trimmedUrl = value.trim();
        if (trimmedUrl.isEmpty()) {
            return true;
        }

        String decodedUrl = decodeUrl(trimmedUrl);
        ValidationResult validationResult = doValidate(decodedUrl);

        if (validationResult != ValidationResult.VALID) {
            System.err.println(
                "[ValidSnapUrlValidator] validation failed: " + validationResult + " / value=" + value
            );

            if (context != null) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(resolveMessage(validationResult))
                    .addConstraintViolation();
            }
        }

        return validationResult == ValidationResult.VALID;
    }

    private String decodeUrl(String sourceUrl) {
        if (!ENCODED_PATTERN.matcher(sourceUrl).find()) {
            return sourceUrl;
        }

        try {
            return URLDecoder.decode(sourceUrl, StandardCharsets.UTF_8.name());
        } catch (IllegalArgumentException exception) {
            return sourceUrl;
        } catch (UnsupportedEncodingException exception) {
            throw new IllegalStateException(
                String.format(
                    "Unable to decode %s: Standard UTF-8 charset is not available.",
                    this.getClass().getSimpleName()
                ),
                exception
            );
        }
    }

    ValidationResult doValidate(String decodedUrl) {
        if (decodedUrl == null || decodedUrl.trim().isEmpty()) {
            return ValidationResult.EMPTY_URL;
        }

        String baseUrl = extractBaseUrl(decodedUrl);

        // 1. URL 기본 형식을 파싱한다.
        URI normalizedUri;
        try {
            normalizedUri = new URI(baseUrl).normalize();
        } catch (URISyntaxException exception) {
            return ValidationResult.INVALID_URL_SYNTAX;
        }

        // 2. scheme을 검증한다.
        ValidationResult schemeValidationResult = validateScheme(normalizedUri);
        if (schemeValidationResult != ValidationResult.VALID) {
            return schemeValidationResult;
        }

        // 3. host 존재 여부를 검증한다.
        ValidationResult hostValidationResult = validateHost(normalizedUri);
        if (hostValidationResult != ValidationResult.VALID) {
            return hostValidationResult;
        }

        String host = normalizedUri.getHost();

        // 4. host를 실제 IP로 resolve한다.
        InetAddress resolvedAddress;
        try {
            resolvedAddress = InetAddress.getByName(host);
        } catch (UnknownHostException exception) {
            return ValidationResult.DNS_LOOKUP_FAILED;
        } catch (Exception exception) {
            return ValidationResult.DNS_LOOKUP_FAILED;
        }

        // 5. resolve된 IP가 내부망, 클라우드 메타데이터 등의 주소인지 검증한다.
        return validateResolvedIp(resolvedAddress, resolvedAddress.getHostAddress());
    }

    private ValidationResult validateScheme(URI normalizedUri) {
        String scheme = normalizedUri.getScheme();
        if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
            return ValidationResult.INVALID_SCHEME;
        }

        return ValidationResult.VALID;
    }

    private ValidationResult validateHost(URI normalizedUri) {
        return normalizedUri.getHost() == null
            ? ValidationResult.HOST_NOT_FOUND
            : ValidationResult.VALID;
    }

    private String extractBaseUrl(String sourceUrl) {
        int queryIndex = sourceUrl.indexOf('?');
        int fragmentIndex = sourceUrl.indexOf('#');

        int splitIndex = sourceUrl.length();

        if (queryIndex >= 0) {
            splitIndex = Math.min(splitIndex, queryIndex);
        }

        if (fragmentIndex >= 0) {
            splitIndex = Math.min(splitIndex, fragmentIndex);
        }

        return sourceUrl.substring(0, splitIndex);
    }

    private ValidationResult validateResolvedIp(InetAddress address, String resolvedIp) {
        if (address.isAnyLocalAddress()) {
            return ValidationResult.UNSPECIFIED_IP;
        }

        if (address.isLoopbackAddress() || resolvedIp.equals("127.0.0.1") || resolvedIp.equals("0.0.0.0")) {
            return ValidationResult.LOOPBACK_IP;
        }

        if (address.isSiteLocalAddress()) {
            return ValidationResult.PRIVATE_IP;
        }

        if (address.isLinkLocalAddress() || resolvedIp.startsWith("169.254")) {
            return ValidationResult.METADATA_IP;
        }

        if (address.isMulticastAddress()) {
            return ValidationResult.MULTICAST_IP;
        }

        // IPv6 사설 주소 대역인 fc, fd prefix는 별도로 차단한다.
        if (address.getAddress().length == 16 && (resolvedIp.startsWith("fc") || resolvedIp.startsWith("fd"))) {
            return ValidationResult.IPV6_ULA;
        }

        return ValidationResult.VALID;
    }

    private String resolveMessage(ValidationResult validationResult) {
        switch (validationResult) {
            case EMPTY_URL:
                return "URL을 입력해 주세요";
            case INVALID_SCHEME:
                return "http 또는 https URL만 입력할 수 있습니다";
            default:
                return "등록할 수 없는 URL입니다";
        }
    }

    enum ValidationResult {
        VALID,
        EMPTY_URL,
        INVALID_URL_SYNTAX,
        INVALID_SCHEME,
        HOST_NOT_FOUND,
        DNS_LOOKUP_FAILED,
        UNSPECIFIED_IP,
        LOOPBACK_IP,
        PRIVATE_IP,
        METADATA_IP,
        MULTICAST_IP,
        IPV6_ULA
    }
}
