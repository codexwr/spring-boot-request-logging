package com.github.codexwr.springbootrequestlogging.configuration;

import com.github.codexwr.springbootrequestlogging.component.*;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
class DefaultLogPrinter implements LogPrinter {
    private final Logger logger = LoggerFactory.getLogger("LogPrinter");

    private final LoggingFilterProperties properties;
    private final HttpHeaderMask headerMask;
    private final RequestBodyMask requestBodyMask;
    private final ResponseBodyMask responseBodyMask;
    private final UsernameProvider usernameProvider;

    @Nullable
    @Override
    public UsernameProvider usernameProvider() {
        return usernameProvider;
    }

    @Override
    public void request(@Nonnull HttpMethod httpMethod, @Nonnull String url, @Nullable String queryString, @Nullable String remoteAddr, @Nullable String sessionId, @Nullable HttpHeaders header, @Nullable MediaType bodyContentType, @Nullable String body) {
        Assert.notNull(httpMethod, "httpMethod must not be null");
        Assert.notNull(url, "url must not be null");

        final var msg = new StringBuilder();
        if (StringUtils.hasText(properties.getEnterPrefixDecor()))
            msg.append(properties.getEnterPrefixDecor());

        assembleUrl(msg, httpMethod, url, queryString);
        assembleClientInfo(msg, remoteAddr, sessionId);
        assembleHeader(msg, httpMethod, url, header);

        logger.info(msg.toString());
    }

    @Override
    public void response(@Nullable Long executionTime, @Nonnull HttpStatusCode httpStatus, @Nonnull HttpMethod httpMethod, @Nonnull String url, @Nullable String queryString, @Nullable MediaType requestBodyContentType, @Nullable String requestBody, @Nullable MediaType responseBodyContentType, @Nullable String responseBody) {
        Assert.notNull(httpStatus, "httpStatus must not be null");
        Assert.notNull(httpMethod, "httpMethod must not be null");
        Assert.notNull(url, "url must not be null");

        final var msg = new StringBuilder();
        if (StringUtils.hasText(properties.getExitPrefixDecor()))
            msg.append(properties.getExitPrefixDecor());

        assembleResponseInfo(msg, executionTime, httpStatus);
        assembleUrl(msg, httpMethod, url, queryString);
        assembleUsername(msg);
        assembleRequestBody(msg, httpMethod, url, requestBodyContentType, requestBody);
        assembleResponseBody(msg, httpMethod, url, responseBodyContentType, responseBody);

        logger.info(msg.toString());
    }

    private void assembleResponseInfo(StringBuilder msg, Long executionTime, HttpStatusCode httpStatus) {
        msg.append("<").append(httpStatus);
        if (executionTime != null)
            msg.append(":").append(executionTime).append("ms");
        msg.append("> ");
    }

    private void assembleHeader(StringBuilder msg, HttpMethod httpMethod, String url, HttpHeaders header) {
        if (!properties.isIncludeHeaders() || header == null) return;

        var maskHeader = headerMask.getMaskingHeaders(httpMethod, url, header);
        msg.append(", headers=").append(maskHeader);
    }

    private void assembleRequestBody(StringBuilder msg, HttpMethod httpMethod, String url, MediaType bodyContentType, String body) {
        if (!properties.isIncludeRequestBody() || !StringUtils.hasText(body)) return;

        var maskBody = requestBodyMask.getMaskBody(httpMethod, url, bodyContentType, body);
        msg.append(", requestBody=").append(maskBody);

    }

    private void assembleResponseBody(StringBuilder msg, HttpMethod httpMethod, String url, MediaType bodyContentType, String body) {
        if (!properties.isIncludeResponseBody() || !StringUtils.hasText(body)) return;

        var maskBody = responseBodyMask.getMaskBody(httpMethod, url, bodyContentType, body);
        msg.append(", responseBody=").append(maskBody);
    }

    private void assembleUsername(StringBuilder msg) {
        if (!properties.isIncludeClientInfo()) return;

        var usernameProvider = usernameProvider();
        var username = usernameProvider != null ? usernameProvider.getUsername() : null;
        if (StringUtils.hasText(username))
            msg.append(", username=").append(username);
    }

    private void assembleClientInfo(StringBuilder msg, String remoteAddr, String sessionId) {
        if (!properties.isIncludeClientInfo()) return;

        if (StringUtils.hasText(remoteAddr))
            msg.append(", ip=").append(remoteAddr);

        if (StringUtils.hasText(sessionId))
            msg.append(", sessionId=").append(sessionId);
    }

    private void assembleUrl(StringBuilder msg, HttpMethod httpMethod, String url, String queryString) {
        msg.append(httpMethod.name()).append(" ").append(url);

        if (properties.isIncludeQueryString() && StringUtils.hasText(queryString))
            msg.append("?").append(queryString);
    }
}
