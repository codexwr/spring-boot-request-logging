package com.github.codexwr.springbootrequestlogging.reactor;

import com.github.codexwr.springbootrequestlogging.configuration.LoggingFilterAutoConfigurationSteps;
import com.github.codexwr.springbootrequestlogging.servlet.ServletLoggingFilterAutoConfigurationSteps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ReactiveWebApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReactiveLoggingFilterAutoConfigurationTest {
    private final ReactiveWebApplicationContextRunner reactiveContextRunner = new ReactiveWebApplicationContextRunner()
            .withConfiguration(
                    AutoConfigurations.of(
                            LoggingFilterAutoConfigurationSteps.autoConfigurationClass,
                            ServletLoggingFilterAutoConfigurationSteps.autoConfigurationClass,
                            ReactiveLoggingFilterAutoConfiguration.class)
            );

    @Test
    @DisplayName("Reactive LoggingFilter 로딩")
    void reactiveLoggingFilterLoadingSuccess() {

        reactiveContextRunner.run(context -> assertThat(context.getBean(LoggingFilter.class)).isInstanceOf(DefaultReactiveLoggingFilter.class));
    }

    @Test
    @DisplayName("Servlet LoggingFilter 로딩 실패")
    void servletLoggingFilterLoadingFailed() {
        reactiveContextRunner.run(context -> assertThatThrownBy(() -> context.getBean(com.github.codexwr.springbootrequestlogging.servlet.LoggingFilter.class))
                .isExactlyInstanceOf(NoSuchBeanDefinitionException.class)
        );
    }
}