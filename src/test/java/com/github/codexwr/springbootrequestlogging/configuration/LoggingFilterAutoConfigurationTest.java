package com.github.codexwr.springbootrequestlogging.configuration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class LoggingFilterAutoConfigurationTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(LoggingFilterAutoConfiguration.class));

    @Test
    @DisplayName("설정 로딩")
    void properties() {
        contextRunner.run(context -> assertThat(context).hasSingleBean(LoggingFilterProperties.class));
    }

    @Test
    @DisplayName("usernameProvider 로딩")
    void usernameProvider() {
        contextRunner.run(context -> assertThat(context.getBean(UsernameProvider.class)).isInstanceOf(DefaultUsernameProvider.class));
    }

    @Test
    @DisplayName("ignoreLoggingPath 로딩")
    void ignoreLoggingPath() {
        contextRunner.run(context -> assertThat(context.getBean(IgnoreLoggingPath.class)).isInstanceOf(DefaultIgnoreLoggingPath.class));
    }

    @Test
    @DisplayName("httpHeaderMask 로딩")
    void httpHeaderMask() {
        contextRunner.run(context -> assertThat(context.getBean(HttpHeaderMask.class)).isInstanceOf(DefaultHttpHeaderMask.class));
    }

    @Test
    @DisplayName("requestBodyMask 로딩")
    void requestBodyMask() {
        contextRunner.run(context -> assertThat(context.getBean(RequestBodyMask.class)).isInstanceOf(DefaultRequestBodyMask.class));
    }

    @Test
    @DisplayName("responseBodyMask 로딩")
    void responseBodyMask() {
        contextRunner.run(context -> assertThat(context.getBean(ResponseBodyMask.class)).isInstanceOf(DefaultResponseBodyMask.class));
    }

    @Test
    @DisplayName("logPrinter 로딩")
    void logPrinter() {
        contextRunner.run(context -> assertThat(context.getBean(LogPrinter.class)).isInstanceOf(DefaultLogPrinter.class));
    }
}