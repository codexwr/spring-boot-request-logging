package com.github.codexwr.springbootrequestlogging.configuration;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;

import java.util.Map;

public interface LogPrinter {
    @Nullable
    UsernameProvider usernameProvider();

    void request(@Nonnull LogItem logItem);

    void response(@Nullable Long executionTime, @Nonnull HttpStatusCode httpStatus, @Nonnull LogItem logItem);

    record LogItem(@Nonnull HttpMethod httpMethod, @Nonnull String url, @Nullable String queryString,
                   @Nullable String remoteAddr, @Nullable String sessionId, @Nullable HttpHeaders header,
                   @Nullable MediaType requestBodyContentType, @Nullable String requestBody,
                   @Nullable MediaType responseBodyContentType, @Nullable String responseBody,
                   @Nullable Map<String, String> extraInfo) {
        public LogItem {
            Assert.notNull(httpMethod, "httpMethod must not be null");
            Assert.notNull(url, "url must not be null");
        }
    }
}
