package org.ringling.backend.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ValidSnapUrlValidatorTest {

    private final ValidSnapUrlValidator validator = new ValidSnapUrlValidator();

    @Test
    @DisplayName("공개 URL은 검증에 성공한다")
    void 공개_URL은_검증에_성공한다() {
        // given: 외부에서 접근 가능한 정상 URL
        String url = "https://search.naver.com/search.naver?query=인코딩";

        // when: URL 검증을 수행한다
        boolean result = validator.isValid(url, null);

        // then: 정상 URL이므로 검증에 성공해야 한다
        assertTrue(result);
    }

    @Test
    @DisplayName("정상 URL을 인코딩한 입력은 검증에 성공한다")
    void 정상_URL을_인코딩한_입력은_검증에_성공한다() {
        // given: 퍼센트 인코딩된 정상 URL
        String url = "https%3A%2F%2Fsearch.naver.com%2Fsearch.naver%3Fquery%3D%EC%9D%B8%EC%BD%94%EB%94%A9"; // https://search.naver.com/search.naver?query=인코딩

        // when: URL 검증을 수행한다
        boolean result = validator.isValid(url, null);

        // then: 디코딩 후 정상 URL로 해석되므로 검증에 성공해야 한다
        assertTrue(result);
    }

    @Test
    @DisplayName("루프백 주소는 검증에 실패한다")
    void 루프백_주소는_검증에_실패한다() {
        // given: 내부망 접근을 유도하는 loopback 주소
        String url = "http://127.0.0.1";

        // when: URL 검증을 수행한다
        boolean result = validator.isValid(url, null);

        // then: SSRF 방어를 위해 검증에 실패해야 한다
        assertFalse(result);
    }

    @Test
    @DisplayName("메타데이터 IP는 검증에 실패한다")
    void 메타데이터_IP는_검증에_실패한다() {
        // given: 클라우드 메타데이터 접근을 유도하는 IP
        String url = "http://169.254.169.254";

        // when: URL 검증을 수행한다
        boolean result = validator.isValid(url, null);

        // then: 메타데이터 IP는 SSRF 방어를 위해 차단해야 한다
        assertFalse(result);
    }

    @Test
    @DisplayName("정상 공개 URL은 VALID 결과를 반환한다")
    void 정상_공개_URL은_VALID_결과를_반환한다() {
        // given: 외부에서 접근 가능한 정상 URL
        String url = "https://www.naver.com";

        // when: 내부 검증 결과를 확인한다
        ValidSnapUrlValidator.ValidationResult result = validator.doValidate(url);

        // then: 정상 URL이므로 VALID여야 한다
        assertEquals(ValidSnapUrlValidator.ValidationResult.VALID, result);
    }

    @Test
    @DisplayName("공백 문자열은 EMPTY_URL 결과를 반환한다")
    void 공백_문자열은_EMPTY_URL_결과를_반환한다() {
        // given: 공백만 포함한 URL 문자열
        String url = "   ";

        // when: 내부 검증 결과를 확인한다
        ValidSnapUrlValidator.ValidationResult result = validator.doValidate(url);

        // then: 빈 URL 입력으로 분류되어야 한다
        assertEquals(ValidSnapUrlValidator.ValidationResult.EMPTY_URL, result);
    }

    @Test
    @DisplayName("잘못된 scheme은 INVALID_SCHEME 결과를 반환한다")
    void 잘못된_scheme은_INVALID_SCHEME_결과를_반환한다() {
        // given: 허용하지 않는 scheme을 사용하는 URL
        String url = "ftp://www.naver.com";

        // when: 내부 검증 결과를 확인한다
        ValidSnapUrlValidator.ValidationResult result = validator.doValidate(url);

        // then: 잘못된 scheme으로 분류되어야 한다
        assertEquals(ValidSnapUrlValidator.ValidationResult.INVALID_SCHEME, result);
    }

    @Test
    @DisplayName("비정상 URL 형식은 HOST_NOT_FOUND 결과를 반환한다")
    void 비정상_URL_형식은_HOST_NOT_FOUND_결과를_반환한다() {
        // given: host를 정상적으로 해석할 수 없는 URL
        String url = "http:/www.naver.com";

        // when: 내부 검증 결과를 확인한다
        ValidSnapUrlValidator.ValidationResult result = validator.doValidate(url);

        // then: host를 추출할 수 없는 형식으로 분류되어야 한다
        assertEquals(ValidSnapUrlValidator.ValidationResult.HOST_NOT_FOUND, result);
    }

    @Test
    @DisplayName("존재하지 않는 도메인은 DNS_LOOKUP_FAILED 결과를 반환한다")
    void 존재하지_않는_도메인은_DNS_LOOKUP_FAILED_결과를_반환한다() {
        // given: DNS 조회가 실패해야 하는 존재하지 않는 도메인
        String url = "https://this-domain-should-not-exist-123456789.com";

        // when: 내부 검증 결과를 확인한다
        ValidSnapUrlValidator.ValidationResult result = validator.doValidate(url);

        // then: DNS 조회 실패로 분류되어야 한다
        assertEquals(ValidSnapUrlValidator.ValidationResult.DNS_LOOKUP_FAILED, result);
    }

    @Test
    @DisplayName("루프백 주소는 LOOPBACK_IP 결과를 반환한다")
    void 루프백_주소는_LOOPBACK_IP_결과를_반환한다() {
        // given: 내부망 접근을 유도하는 loopback 주소
        String url = "http://127.0.0.1";

        // when: 내부 검증 결과를 확인한다
        ValidSnapUrlValidator.ValidationResult result = validator.doValidate(url);

        // then: loopback 주소로 분류되어야 한다
        assertEquals(ValidSnapUrlValidator.ValidationResult.LOOPBACK_IP, result);
    }

    @Test
    @DisplayName("사설망 주소는 PRIVATE_IP 결과를 반환한다")
    void 사설망_주소는_PRIVATE_IP_결과를_반환한다() {
        // given: 사설망 대역에 속하는 주소
        String url = "http://10.0.0.1";

        // when: 내부 검증 결과를 확인한다
        ValidSnapUrlValidator.ValidationResult result = validator.doValidate(url);

        // then: 사설망 주소로 분류되어야 한다
        assertEquals(ValidSnapUrlValidator.ValidationResult.PRIVATE_IP, result);
    }

    @Test
    @DisplayName("메타데이터 IP는 METADATA_IP 결과를 반환한다")
    void 메타데이터_IP는_METADATA_IP_결과를_반환한다() {
        // given: 클라우드 메타데이터 접근을 유도하는 IP
        String url = "http://169.254.169.254";

        // when: 내부 검증 결과를 확인한다
        ValidSnapUrlValidator.ValidationResult result = validator.doValidate(url);

        // then: 메타데이터 IP로 분류되어야 한다
        assertEquals(ValidSnapUrlValidator.ValidationResult.METADATA_IP, result);
    }

    @Test
    @DisplayName("멀티캐스트 주소는 MULTICAST_IP 결과를 반환한다")
    void 멀티캐스트_주소는_MULTICAST_IP_결과를_반환한다() {
        // given: 멀티캐스트 범위에 속하는 주소
        String url = "http://224.0.0.1";

        // when: 내부 검증 결과를 확인한다
        ValidSnapUrlValidator.ValidationResult result = validator.doValidate(url);

        // then: 멀티캐스트 주소로 분류되어야 한다
        assertEquals(ValidSnapUrlValidator.ValidationResult.MULTICAST_IP, result);
    }

    @Test
    @DisplayName("IPv6 미지정 주소는 UNSPECIFIED_IP 결과를 반환한다")
    void IPv6_미지정_주소는_UNSPECIFIED_IP_결과를_반환한다() {
        // given: 모든 인터페이스를 의미하는 IPv6 unspecified address
        String url = "http://[::]";

        // when: 내부 검증 결과를 확인한다
        ValidSnapUrlValidator.ValidationResult result = validator.doValidate(url);

        // then: 미지정 주소로 분류되어야 한다
        assertEquals(ValidSnapUrlValidator.ValidationResult.UNSPECIFIED_IP, result);
    }

    @Test
    @DisplayName("IPv6 ULA 주소는 IPV6_ULA 결과를 반환한다")
    void IPv6_ULA_주소는_IPV6_ULA_결과를_반환한다() {
        // given: 사설망 범위인 IPv6 ULA 주소
        String url = "http://[fc00::1]";

        // when: 내부 검증 결과를 확인한다
        ValidSnapUrlValidator.ValidationResult result = validator.doValidate(url);

        // then: IPv6 ULA 주소로 분류되어야 한다
        assertEquals(ValidSnapUrlValidator.ValidationResult.IPV6_ULA, result);
    }
}
