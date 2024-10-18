package com.github.codexwr.springbootrequestlogging.configuration;

import com.github.codexwr.springbootrequestlogging.component.LogPrinter;
import com.github.codexwr.springbootrequestlogging.component.*;
import com.github.codexwr.springbootrequestlogging.reactor.WebfluxLoggingFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(LoggingFilterProperties.class)
@ConditionalOnProperty(name = {LoggingFilterProperties.ENABLED}, havingValue = "true", matchIfMissing = true)
class LoggingFilterAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    UsernameProvider usernameProvider() {
        return new DefaultUsernameProvider();
    }

    @Bean
    @ConditionalOnMissingBean
    IgnoreLoggingPath ignoreLoggingPath(LoggingFilterProperties properties) {
        return new DefaultIgnoreLoggingPath(properties.getExcludeLoggingPaths());
    }

    @Bean
    @ConditionalOnMissingBean
    HttpHeaderMask httpHeaderMask(LoggingFilterProperties properties) {
        return new DefaultHttpHeaderMask(properties.getMaskString(), properties.getDefaultHeaderMasks(), properties.getPathHeaderMask());
    }

    @Bean
    @ConditionalOnMissingBean
    RequestBodyMask requestBodyMask(LoggingFilterProperties properties) {
        return new DefaultRequestBodyMask(properties.getMaskString(), properties.getRequestJsonBodyMasks(), properties.getRequestFormDataMasks());
    }

    @Bean
    @ConditionalOnMissingBean
    ResponseBodyMask responseBodyMask(LoggingFilterProperties properties) {
        return new DefaultResponseBodyMask(properties.getMaskString(), properties.getResponseJsonBodyMasks());
    }

    @Bean
    @ConditionalOnMissingBean
    LogPrinter logPrinter(LoggingFilterProperties properties, HttpHeaderMask httpHeaderMask, RequestBodyMask requestBodyMask, ResponseBodyMask responseBodyMask, UsernameProvider usernameProvider) {
        return new DefaultLogPrinter(properties, httpHeaderMask, requestBodyMask, responseBodyMask, usernameProvider);
    }

    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    WebfluxLoggingFilter webfluxLoggingFilter(LogPrinter logPrinter, IgnoreLoggingPath ignoreLoggingPath, LoggingFilterProperties properties) {
        return new WebfluxLoggingFilter(logPrinter, ignoreLoggingPath, properties.isIncludeRequestBody(), properties.isIncludeResponseBody(), properties.getFilterOrder());
    }
}
