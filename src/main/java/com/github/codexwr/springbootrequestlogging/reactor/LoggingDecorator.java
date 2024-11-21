package com.github.codexwr.springbootrequestlogging.reactor;

import jakarta.annotation.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.web.util.WebUtils;

import java.nio.charset.Charset;

interface LoggingDecorator {
    default boolean isCompatibleMediaType(MediaType mediaType) {
        var contentType = getContentType();
        return contentType != null && contentType.isCompatibleWith(mediaType);
    }

    default Charset getCharset() {
        var contentType = getContentType();

        Charset charset = contentType != null ? contentType.getCharset() : null;

        return charset != null ? charset : Charset.forName(WebUtils.DEFAULT_CHARACTER_ENCODING);
    }

    default MediaType getContentType() {
        MediaType contentType = null;
        try {
            contentType = getHeaders().getContentType();
        } catch (Exception ignored) {

        }

        return contentType;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    default <T> T getNativeRequest(ServerHttpRequest request, @Nullable Class<T> requiredType) {
        if (requiredType == null) return null;

        if (requiredType.isInstance(request)) return (T) request;

        if (request instanceof ServerHttpRequestDecorator req)
            return getNativeRequest(req.getDelegate(), requiredType);

        return null;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    default <T> T getNativeResponse(ServerHttpResponse response, @Nullable Class<T> requiredType) {
        if (requiredType == null) return null;

        if (requiredType.isInstance(response)) return (T) response;

        if (response instanceof ServerHttpResponseDecorator res)
            return getNativeResponse(res.getDelegate(), requiredType);

        return null;
    }

    HttpHeaders getHeaders();
}
