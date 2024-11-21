package com.github.codexwr.springbootrequestlogging.servlet;

import com.github.codexwr.springbootrequestlogging.configuration.LoggingFilterAutoConfigurationSteps;
import com.github.codexwr.springbootrequestlogging.reactor.ReactiveLoggingFilterAutoConfigurationSteps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServletLoggingFilterAutoConfigurationTest {
    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(
                    AutoConfigurations.of(LoggingFilterAutoConfigurationSteps.autoConfigurationClass,
                            ReactiveLoggingFilterAutoConfigurationSteps.autoConfigurationClass,
                            ServletLoggingFilterAutoConfiguration.class
                    )
            );

    @Test
    @DisplayName("Servlet LoggingFilter 로딩")
    void servletLoggingFilterLoadingSuccess() {
        contextRunner.run(context -> assertThat(context.getBean(LoggingFilter.class)).isInstanceOf(DefaultServletLoggingFilter.class));
    }

    @Test
    @DisplayName("Reactive LoggingFilter 로딩 실패 ")
    void reactiveLoggingFilterLoadingFailed() {
        contextRunner.run(context -> assertThatThrownBy(() -> context.getBean(com.github.codexwr.springbootrequestlogging.reactor.LoggingFilter.class))
                .isInstanceOf(NoSuchBeanDefinitionException.class));
    }
}