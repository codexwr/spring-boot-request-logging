package com.github.codexwr.springbootrequestlogging.reactor;

import com.github.codexwr.springbootrequestlogging.configuration.LogPrinter;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

interface LoggingDecorator {
    default boolean isCompatibleMediaType(MediaType mediaType) {
        var contentType = getContentType();
        return contentType != null && contentType.isCompatibleWith(mediaType);
    }

    default Charset getCharset() {
        var contentType = getContentType();

        Charset charset = contentType != null ? contentType.getCharset() : null;

        return charset != null ? charset : StandardCharsets.UTF_8;
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

    default Mono<LogPrinter.LogItem> getLogItem(@Nullable ServerWebExchange exchange, @Nonnull LoggingRequestDecorator requestDecorator, @Nullable LoggingResponseDecorator responseDecorator, @Nullable Map<String, String> extraInfo) {
        var sessionIdStream = Mono.defer(() -> {
            if (exchange == null) return Mono.just(Optional.<String>empty());
            return exchange.getSession()
                    .filter(WebSession::isStarted)
                    .map(session -> Optional.of(session.getId()))
                    .defaultIfEmpty(Optional.empty());
        });
        return Mono.zip(sessionIdStream, requestDecorator.getRequestBody(), (sessionId, body) -> new LogPrinter.LogItem(
                        requestDecorator.getMethod(),
                        requestDecorator.getURI().getPath(),
                        requestDecorator.getURI().getQuery(),
                        requestDecorator.getRemoteAddress() != null ? requestDecorator.getRemoteAddress().getHostString() : null,
                        sessionId.orElse(null),
                        requestDecorator.getHeaders(),
                        requestDecorator.getContentType(),
                        body.orElse(null),
                        responseDecorator != null ? responseDecorator.getContentType() : null,
                        responseDecorator != null ? responseDecorator.getResponseBody() : null,
                        extraInfo
                )
        );
    }
}
