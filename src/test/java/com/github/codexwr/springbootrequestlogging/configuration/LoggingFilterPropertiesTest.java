package com.github.codexwr.springbootrequestlogging.configuration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.atIndex;

@SpringBootTest(classes = {LoggingFilterProperties.class})
@EnableConfigurationProperties(LoggingFilterProperties.class)
@ActiveProfiles("test-props")
public class LoggingFilterPropertiesTest {
    @Autowired
    @Qualifier("loggingFilterProperties")
    private LoggingFilterProperties props;

    @Test
    @DisplayName("로깅 제외 설정값 확인")
    void checkExcludeLoggingPaths() {
        // given classpath:application-test-props.yml

        // then
        assertThat(props.getExcludeLoggingPaths())
                .hasSize(2)
                .satisfies(path -> {
                    assertThat(path.getMethod()).isEqualTo(HttpMethod.POST);
                    assertThat(path.getPathPatterns())
                            .hasSize(2)
                            .anyMatch(it -> it.equals("/api/url/2"));
                }, atIndex(0))
                .satisfies(path -> {
                    assertThat(path.getMethod()).isEqualTo(HttpMethod.GET);
                    assertThat(path.getPathPatterns())
                            .hasSize(2)
                            .anyMatch(it -> it.equals("/api-docs/**"));
                }, atIndex(1));
    }

    @Test
    @DisplayName("헤더 마스킹 설정값 확인")
    void checkHeaderMask() {
        // given classpath:application-test-props.yml

        // then
        assertThat(props.getPathHeaderMask())
                .hasSize(2)
                .satisfies(path -> {
                    assertThat(path.getMethod()).isEqualTo(HttpMethod.POST);
                    assertThat(path.getPathPattern()).isEqualTo("/api/member");
                    assertThat(path.getMaskKey())
                            .hasSize(2)
                            .anyMatch(it -> it.equals("Postman-Token"));
                }, atIndex(0))
                .satisfies(path -> {
                    assertThat(path.getMethod()).isEqualTo(HttpMethod.GET);
                    assertThat(path.getPathPattern()).isEqualTo("/api/member/*");
                    assertThat(path.getMaskKey())
                            .hasSize(2)
                            .anyMatch(it -> it.equals("Authorization"));
                }, atIndex(1));
    }

    @Test
    @DisplayName("JSON 바디 마스킹 설정값 확인")
    void checkRequestBody() {
        // given classpath:application-test-props.yml

        // then
        assertThat(props.getRequestJsonBodyMasks())
                .hasSize(2)
                .satisfies(path -> {
                    assertThat(path.getMethod()).isEqualTo(HttpMethod.GET);
                    assertThat(path.getPathPattern()).isEmpty();
                    assertThat(path.getMaskJson()).isEmpty();
                }, atIndex(1))
                .first()
                .satisfies(path -> {
                    assertThat(path.getMethod()).isEqualTo(HttpMethod.POST);
                    assertThat(path.getPathPattern()).isEqualTo("/api/auth/**");
                    assertThat(path.getMaskJson())
                            .hasSize(2)
                            .anyMatch(it -> it.equals("$.name.*"));
                });
    }
}
