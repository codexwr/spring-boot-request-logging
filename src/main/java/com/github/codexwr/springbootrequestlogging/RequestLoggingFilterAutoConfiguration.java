package com.github.codexwr.springbootrequestlogging;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.Filter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnProperty(name = {RequestLoggingFilterProperties.REQUEST_LOGGING_FILTER_ENABLED}, matchIfMissing = true)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableConfigurationProperties(RequestLoggingFilterProperties.class)
class RequestLoggingFilterAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    PayloadMessageReader payloadMessageReader(ObjectMapper objectMapper) {
        return new PayloadMessageReaderImpl(objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    RequestLoggingFilter requestLoggingFilter(RequestLoggingFilterProperties properties, PayloadMessageReader payloadMessageReader) {
        return new RequestLoggingFilter(properties, payloadMessageReader);
    }

    @Bean
    @ConditionalOnMissingBean
    FilterRegistrationBean<Filter> requestLoggingFilterBean(RequestLoggingFilterProperties properties, RequestLoggingFilter requestLoggingFilter) {
        final var filter = new FilterRegistrationBean<>();
        filter.setFilter(requestLoggingFilter);
        filter.setOrder(properties.getFilterOrder());
        filter.addUrlPatterns("/*");

        return filter;
    }
}
