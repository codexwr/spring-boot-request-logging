package com.github.codexwr.springbootrequestlogging.servlet;

import com.github.codexwr.springbootrequestlogging.configuration.LogPrinter;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServletServerHttpRequest;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

interface LoggingWrapper {
    Logger log = LoggerFactory.getLogger(LoggingWrapper.class);

    default boolean isCompatibleMediaType(MediaType mediaType) {
        var contentType = getContentType(getContentType());
        return contentType != null && contentType.isCompatibleWith(mediaType);
    }

    default MediaType getContentType(String contentType) {
        try {
            return MediaType.valueOf(contentType);
        } catch (Exception e) {
            log.trace("contentType parsing error.", e);
            return null;
        }
    }

    default Charset getCharset(String encodeCharset) {
        Charset charset = null;
        try {
            charset = Charset.forName(encodeCharset);
        } catch (Exception e) {
            log.trace("charset parsing error.", e);
        }

        return charset != null ? charset : StandardCharsets.UTF_8;
    }

    String getContentType();

    default LogPrinter.LogItem getLogItem(@Nonnull LoggingRequestWrapper request, @Nullable LoggingResponseWrapper response, @Nullable Map<String, String> extraInfo) {
        return new LogPrinter.LogItem(
                request.getHttpMethod(),
                request.getRequestURI(),
                request.getQueryString(),
                request.getRemoteAddr(),
                request.getSession(false) != null ? request.getSession(false).getId() : null,
                new ServletServerHttpRequest(request).getHeaders(),
                getContentType(request.getContentType()),
                request.getRequestBody(),
                response != null ? getContentType(response.getContentType()) : null,
                response != null ? response.getResponseBody() : null,
                extraInfo
        );
    }
}
