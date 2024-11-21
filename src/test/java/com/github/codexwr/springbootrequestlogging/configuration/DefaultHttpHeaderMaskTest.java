package com.github.codexwr.springbootrequestlogging.configuration;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import java.util.List;
import java.util.Set;

class DefaultHttpHeaderMaskTest {
    @Test
    @DisplayName("기본 마스크 동작 - 마스킹도 불일치")
    void defaultNoMaks() {
        // given
        var method = HttpMethod.GET;
        var path = "/def/no-masking";
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "bearer token");
        headers.add("X-Request-ID", "As5cm9Dxl8wg9m");

        // when
        var maskHeaders = HttpHeaderMaskSteps.defaultHttpHeaderMask
                .getMaskingHeaders(method, path, headers);

        // then
        Assertions.assertThat(maskHeaders).hasSize(headers.size())
                .doesNotContainValue(List.of(HttpHeaderMaskSteps.maskOverlay));
    }

    @Test
    @DisplayName("기본 마스크 동작 - 마스킹 일치")
    void defaultMask() {
        // given
        var method = HttpMethod.GET;
        var path = "/def/no-masking";
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "bearer token");
        headers.add("Authorization", "basic");
        headers.add("X-Request-ID", "As5cm9Dxl8wg9m");
        headers.add("Access-Control-Allow-Origin", "*");
        headers.add("Accept-Encoding", "gzip");
        headers.add("Accept-Encoding", "deflate");

        var maskHeaders = HttpHeaderMaskSteps.defaultHttpHeaderMask
                .getMaskingHeaders(method, path, headers);

        // then
        Assertions.assertThat(maskHeaders).hasSize(headers.size())
                .hasEntrySatisfying("Authorization", v -> Assertions.assertThat(v).isEqualTo(headers.get("Authorization")))
                .hasEntrySatisfying("X-Request-ID", v -> Assertions.assertThat(v).isEqualTo(headers.get("X-Request-ID")))
                .hasEntrySatisfying("Access-Control-Allow-Origin", v -> Assertions.assertThat(v).containsOnly(HttpHeaderMaskSteps.maskOverlay))
                .hasEntrySatisfying("Accept-Encoding", v -> Assertions.assertThat(v).containsOnly(HttpHeaderMaskSteps.maskOverlay));
    }

    @Test
    @DisplayName("기본 마스크 동작 - 마스킹 일치(대소문자 구분없음)")
    void defaultMaskCaseInsensitive() {
        // given
        var method = HttpMethod.GET;
        var path = "/def/no-masking";
        HttpHeaders headers = new HttpHeaders();
        headers.add("authorization", "bearer token");
        headers.add("authorization", "basic");
        headers.add("x-request-id", "As5cm9Dxl8wg9m");
        headers.add("ACCESS-CONTROL-ALLOW-ORIGIN", "*");
        headers.add("accept-encoding", "gzip");
        headers.add("accept-encoding", "deflate");

        var maskHeaders = HttpHeaderMaskSteps.defaultHttpHeaderMask
                .getMaskingHeaders(method, path, headers);

        // then
        Assertions.assertThat(maskHeaders).hasSize(headers.size())
                .hasEntrySatisfying("authorization", v -> Assertions.assertThat(v).isEqualTo(headers.get("authorization")))
                .hasEntrySatisfying("x-request-id", v -> Assertions.assertThat(v).isEqualTo(headers.get("X-Request-ID")))
                .hasEntrySatisfying("ACCESS-CONTROL-ALLOW-ORIGIN", v -> Assertions.assertThat(v).containsOnly(HttpHeaderMaskSteps.maskOverlay))
                .hasEntrySatisfying("accept-encoding", v -> Assertions.assertThat(v).containsOnly(HttpHeaderMaskSteps.maskOverlay));
    }

    @Test
    @DisplayName("경로 마스크 동작 - 마스킹 불일치")
    void pathNoMask() {
        // given
        var method = HttpMethod.GET;
        var path = "/users";
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "bearer token");
        headers.add("Authorization", "basic");
        headers.add("==X-Request-ID", "As5cm9Dxl8wg9m");
        headers.add("Access-Control-Allow-Origin", "*");
        headers.add("==Accept-Encoding", "gzip");
        headers.add("==Accept-Encoding", "deflate");

        var maskHeaders = HttpHeaderMaskSteps.defaultHttpHeaderMask
                .getMaskingHeaders(method, path, headers);

        // then
        Assertions.assertThat(maskHeaders).hasSize(headers.size())
                .doesNotContainValue(List.of(HttpHeaderMaskSteps.maskOverlay));
    }

    @Test
    @DisplayName("경로 마스크 동작 - 마스킹 일치1")
    void pathMask1() {
        // given
        var method = HttpMethod.GET;
        var path = "/users";
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "bearer token");
        headers.add("Authorization", "basic");
        headers.add("X-Request-ID", "As5cm9Dxl8wg9m");
        headers.add("Access-Control-Allow-Origin", "*");
        headers.add("Accept-Encoding", "gzip");
        headers.add("Accept-Encoding", "deflate");

        var maskHeaders = HttpHeaderMaskSteps.defaultHttpHeaderMask
                .getMaskingHeaders(method, path, headers);

        // then
        Assertions.assertThat(maskHeaders).hasSize(headers.size())
                .hasEntrySatisfying("Authorization", v -> Assertions.assertThat(v).isEqualTo(headers.get("Authorization")))
                .hasEntrySatisfying("Access-Control-Allow-Origin", v -> Assertions.assertThat(v).isEqualTo(headers.get("Access-Control-Allow-Origin")))
                .hasEntrySatisfying("X-Request-ID", v -> Assertions.assertThat(v).containsOnly(HttpHeaderMaskSteps.maskOverlay))
                .hasEntrySatisfying("Accept-Encoding", v -> Assertions.assertThat(v).containsOnly(HttpHeaderMaskSteps.maskOverlay));
    }

    @Test
    @DisplayName("경로 마스크 동작 - 마스킹 일치2")
    void pathMask2() {
        // given
        var method = HttpMethod.GET;
        var path = "/auth/login/email/password";
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "bearer token");
        headers.add("Authorization", "basic");
        headers.add("X-Request-ID", "As5cm9Dxl8wg9m");
        headers.add("Access-Control-Allow-Origin", "*");
        headers.add("Accept-Encoding", "gzip");
        headers.add("Accept-Encoding", "deflate");

        var maskHeaders = HttpHeaderMaskSteps.defaultHttpHeaderMask
                .getMaskingHeaders(method, path, headers);

        // then
        Assertions.assertThat(maskHeaders).hasSize(headers.size())
                .hasEntrySatisfying("Access-Control-Allow-Origin", v -> Assertions.assertThat(v).isEqualTo(headers.get("Access-Control-Allow-Origin")))
                .hasEntrySatisfying("X-Request-ID", v -> Assertions.assertThat(v).isEqualTo(headers.get("X-Request-ID")))
                .hasEntrySatisfying("Accept-Encoding", v -> Assertions.assertThat(v).isEqualTo(headers.get("Accept-Encoding")))
                .hasEntrySatisfying("Authorization", v -> Assertions.assertThat(v).containsOnly(HttpHeaderMaskSteps.maskOverlay));
    }


    public static class HttpHeaderMaskSteps {
        public static final String maskOverlay = "{{***}}";
        public static final Set<String> defaultMaskKeys = Set.of("Access-Control-Allow-Origin", "Accept-Encoding");
        public static final List<LoggingFilterProperties.PathKeyMask> pathMaskKeys = List.of(
                new LoggingFilterProperties.PathKeyMask(HttpMethod.GET.name(), "/users/**",
                        Set.of("X-Request-ID", "Accept-Encoding")
                ),
                new LoggingFilterProperties.PathKeyMask(HttpMethod.POST.name(), "/auth/login/**",
                        Set.of("Authorization")
                ),
                new LoggingFilterProperties.PathKeyMask(HttpMethod.GET.name(), "/auth/login/**",
                        Set.of("Authorization")
                )
        );

        public static final HttpHeaderMask defaultHttpHeaderMask = new DefaultHttpHeaderMask(maskOverlay, defaultMaskKeys, pathMaskKeys);
    }
}

