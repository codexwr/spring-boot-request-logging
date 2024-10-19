package com.github.codexwr.springbootrequestlogging.configuration;

import com.github.codexwr.springbootrequestlogging.component.*;
import com.github.codexwr.springbootrequestlogging.reactor.WebfluxLoggingFilter;
import com.github.codexwr.springbootrequestlogging.servlet.ServletLoggingFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ReactiveWebApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoggingFilterAutoConfigurationTest {
    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(LoggingFilterAutoConfiguration.class));

    private final ReactiveWebApplicationContextRunner reactiveContextRunner = new ReactiveWebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(LoggingFilterAutoConfiguration.class));

    @Test
    @DisplayName("설정 로딩")
    void properties() {
        contextRunner.run(context -> assertThat(context).hasSingleBean(LoggingFilterProperties.class));
        reactiveContextRunner.run(context -> assertThat(context).hasSingleBean(LoggingFilterProperties.class));
    }

    @Test
    @DisplayName("usernameProvider 로딩")
    void usernameProvider() {
        contextRunner.run(context -> assertThat(context.getBean(UsernameProvider.class)).isInstanceOf(DefaultUsernameProvider.class));
        reactiveContextRunner.run(context -> assertThat(context.getBean(UsernameProvider.class)).isInstanceOf(DefaultUsernameProvider.class));
    }

    @Test
    @DisplayName("ignoreLoggingPath 로딩")
    void ignoreLoggingPath() {
        contextRunner.run(context -> assertThat(context.getBean(IgnoreLoggingPath.class)).isInstanceOf(DefaultIgnoreLoggingPath.class));
        reactiveContextRunner.run(context -> assertThat(context.getBean(IgnoreLoggingPath.class)).isInstanceOf(DefaultIgnoreLoggingPath.class));
    }

    @Test
    @DisplayName("httpHeaderMask 로딩")
    void httpHeaderMask() {
        contextRunner.run(context -> assertThat(context.getBean(HttpHeaderMask.class)).isInstanceOf(DefaultHttpHeaderMask.class));
        reactiveContextRunner.run(context -> assertThat(context.getBean(HttpHeaderMask.class)).isInstanceOf(DefaultHttpHeaderMask.class));
    }

    @Test
    @DisplayName("requestBodyMask 로딩")
    void requestBodyMask() {
        contextRunner.run(context -> assertThat(context.getBean(RequestBodyMask.class)).isInstanceOf(DefaultRequestBodyMask.class));
        reactiveContextRunner.run(context -> assertThat(context.getBean(RequestBodyMask.class)).isInstanceOf(DefaultRequestBodyMask.class));
    }

    @Test
    @DisplayName("responseBodyMask 로딩")
    void responseBodyMask() {
        contextRunner.run(context -> assertThat(context.getBean(ResponseBodyMask.class)).isInstanceOf(DefaultResponseBodyMask.class));
        reactiveContextRunner.run(context -> assertThat(context.getBean(ResponseBodyMask.class)).isInstanceOf(DefaultResponseBodyMask.class));
    }

    @Test
    @DisplayName("logPrinter 로딩")
    void logPrinter() {
        contextRunner.run(context -> assertThat(context.getBean(LogPrinter.class)).isInstanceOf(DefaultLogPrinter.class));
        reactiveContextRunner.run(context -> assertThat(context.getBean(LogPrinter.class)).isInstanceOf(DefaultLogPrinter.class));
    }

    @Test
    @DisplayName("[reactive] webfluxLoggingFilter 로딩")
    void webfluxLoggingFilter() {
        contextRunner.run(context -> assertThatThrownBy(() -> context.getBean(WebfluxLoggingFilter.class))
                .isExactlyInstanceOf(NoSuchBeanDefinitionException.class)
        );
        reactiveContextRunner.run(context -> assertThat(context.getBean(WebfluxLoggingFilter.class)).isInstanceOf(WebfluxLoggingFilter.class));
    }

    @Test
    @DisplayName("[servlet] servletLoggingFilter 로딩")
    void servletLoggingFilter() {
        contextRunner.run(context -> assertThat(context.getBean(ServletLoggingFilter.class)).isInstanceOf(ServletLoggingFilter.class));

        reactiveContextRunner.run(context -> assertThatThrownBy(() -> context.getBean(ServletLoggingFilter.class))
                .isExactlyInstanceOf(NoSuchBeanDefinitionException.class)
        );
    }
}