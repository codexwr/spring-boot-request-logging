package com.github.codexwr.springbootrequestlogging.component;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;

public interface LogPrinter {
    @Nullable
    UsernameProvider usernameProvider();

    void request(@Nonnull HttpMethod httpMethod, @Nonnull String url, @Nullable String queryString, @Nullable String remoteAddr, @Nullable String sessionId, @Nullable HttpHeaders header, @Nullable MediaType bodyContentType, @Nullable String body);

    void response(@Nullable Long executionTime, @Nonnull HttpStatusCode httpStatus, @Nonnull HttpMethod httpMethod, @Nonnull String url, @Nullable String queryString, @Nullable MediaType requestBodyContentType, @Nullable String requestBody, @Nullable MediaType responseBodyContentType, @Nullable String responseBody);
}
