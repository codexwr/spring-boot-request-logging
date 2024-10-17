package com.github.codexwr.springbootrequestlogging.legacy;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.WebUtils;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
public class RequestLoggingFilter extends OncePerRequestFilter {
    private final RequestLoggingFilterProperties properties;
    private final PayloadMessageReader payloadMessageReader;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private List<String> maskingHeaders;

    @PostConstruct
    public void init() {
        if (properties.getMaskingHeaders() != null && !properties.getMaskingHeaders().isEmpty()) {
            maskingHeaders = properties.getMaskingHeaders().stream().map(String::toLowerCase).toList();
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        final var isFirstRequest = !isAsyncDispatch(request);
        var requestToUse = request;
        var responseToUse = response;

        if (isFirstRequest) {
            if (properties.isIncludeRequestPayload() && !(request instanceof ContentCachingRequestWrapper))
                requestToUse = new ContentCachingRequestWrapper(request);
            if (properties.isIncludeResponsePayload() && !(request instanceof ContentCachingResponseWrapper))
                responseToUse = new ContentCachingResponseWrapper(response);
        }

        final var shouldLog = shouldLog(requestToUse);
        if (shouldLog && isFirstRequest) logger.info(createBeforeMessage(requestToUse));

        long executionTime = 0;
        try {
            final var startExecutionTime = System.currentTimeMillis();
            filterChain.doFilter(requestToUse, responseToUse);
            executionTime = System.currentTimeMillis() - startExecutionTime;
        } finally {
            if (!isAsyncStarted(requestToUse)) {
                if (shouldLog) logger.info(createAfterMessage(requestToUse, responseToUse, executionTime));

                final var wrapper = WebUtils.getNativeResponse(responseToUse, ContentCachingResponseWrapper.class);
                if (wrapper != null) {
                    wrapper.copyBodyToResponse();
                }
            }
        }
    }

    protected boolean shouldLog(HttpServletRequest request) {
        if (properties.getExcludeUrlPatterns() == null || properties.getExcludeUrlPatterns().isEmpty()) return true;

        return properties.getExcludeUrlPatterns().stream().noneMatch(it -> pathMatcher.match(it, request.getServletPath()));
    }

    protected String createBeforeMessage(HttpServletRequest request) {
        final var msg = new StringBuilder();
        msg.append(properties.getEnterPrefix());
        msg.append(request.getMethod()).append(' ');
        msg.append(request.getRequestURI());

        if (properties.isIncludeQueryString()) {
            final var queryString = request.getQueryString();
            if (StringUtils.hasText(queryString)) {
                msg.append('?').append(queryString);
            }
        }

        if (properties.isIncludeClientInfo()) {
            final var remoteAddr = request.getRemoteAddr();
            if (StringUtils.hasText(remoteAddr)) {
                msg.append(", client=").append(remoteAddr);
            }

            final var session = request.getSession(false);
            if (session != null) {
                msg.append(", session=").append(session.getId());
            }

            final var remoteUser = request.getRemoteUser();
            if (StringUtils.hasText(remoteUser)) {
                msg.append(", user=").append(remoteUser);
            }
        }

        if (properties.isIncludeHeaders()) {
            msg.append(", headers=").append(maskingHeader(request));
        }

        return msg.toString();
    }

    protected String createAfterMessage(HttpServletRequest request, HttpServletResponse response, long executionTime) {
        final var msg = new StringBuilder();
        msg.append(properties.getExitPrefix());
        msg.append(String.format("<%s:%dms>", HttpStatus.valueOf(response.getStatus()), executionTime)).append(' ');
        msg.append(request.getMethod()).append(' ');
        msg.append(request.getRequestURI());

        if (properties.isIncludeQueryString()) {
            final var queryString = request.getQueryString();
            if (StringUtils.hasText(queryString)) {
                msg.append('?').append(queryString);
            }
        }

        if (properties.isIncludeRequestPayload()) {
            final var payloadMsg = payloadMessageReader.getRequestPayloadMessage(request);
            if (StringUtils.hasText(payloadMsg)) {
                final var outputMsg = printPayloadMessage(payloadMsg, properties.getMaxRequestPayloadSize(), properties.getMaskingRequestPayloadPattern());
                if (outputMsg != null) msg.append(", [requestPayload]=").append(outputMsg);
            }
        }

        if (properties.isIncludeResponsePayload()) {
            final var payloadMsg = payloadMessageReader.getResponsePayloadMessage(response);
            if (StringUtils.hasText(payloadMsg)) {
                final var outputMsg = printPayloadMessage(payloadMsg, properties.getMaxResponsePayloadSize(), properties.getMaskingResponsePayloadPattern());
                if (outputMsg != null) msg.append(", [responsePayload]=").append(outputMsg);
            }
        }

        return msg.toString();
    }

    @Nullable
    private String printPayloadMessage(@Nullable String message, int maxLength, @Nullable Set<String> masks) {
        if (message == null) return null;

        final var masked = maskingMessage(message, masks);

        return masked.substring(0, Math.min(masked.length(), maxLength));
    }

    private HttpHeaders maskingHeader(HttpServletRequest request) {
        final var headers = new ServletServerHttpRequest(request).getHeaders();

        if (maskingHeaders == null || maskingHeaders.isEmpty()) return headers;

        final var names = request.getHeaderNames();
        while (names.hasMoreElements()) {
            final var header = names.nextElement();

            if (maskingHeaders.contains(header.toLowerCase()))
                headers.set(header, properties.getMaskString());
        }

        return headers;
    }

    private String maskingMessage(String message, @Nullable Set<String> masks) {
        if (masks == null || masks.isEmpty()) return message;

        DocumentContext doc;
        try {
            doc = JsonPath.parse(message);
        } catch (Exception ignore) {
            return message;
        }

        masks.forEach(path -> {
            try {
                doc.set(path, properties.getMaskString());
            } catch (Exception ignore) {
            }
        });

        return doc.jsonString();
    }
}
