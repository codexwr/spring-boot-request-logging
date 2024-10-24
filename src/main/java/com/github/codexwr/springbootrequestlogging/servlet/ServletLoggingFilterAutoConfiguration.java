package com.github.codexwr.springbootrequestlogging.servlet;

import com.github.codexwr.springbootrequestlogging.configuration.IgnoreLoggingPath;
import com.github.codexwr.springbootrequestlogging.configuration.LogPrinter;
import com.github.codexwr.springbootrequestlogging.configuration.LoggingFilterProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnProperty(name = {LoggingFilterProperties.ENABLED}, havingValue = "true", matchIfMissing = true)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
class ServletLoggingFilterAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    LoggingFilter servletLoggingFilter(LogPrinter logPrinter, IgnoreLoggingPath ignoreLoggingPath, LoggingFilterProperties properties) {
        return new DefaultServletLoggingFilter(logPrinter, ignoreLoggingPath, properties.isIncludeRequestBody(), properties.isIncludeResponseBody(), properties.getFilterOrder());
    }
}
