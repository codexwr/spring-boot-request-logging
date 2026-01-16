package com.github.codexwr.springbootrequestlogging.reactor;

import com.github.codexwr.springbootrequestlogging.configuration.IgnoreLoggingPath;
import com.github.codexwr.springbootrequestlogging.configuration.LogPrinter;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
class DefaultReactiveLoggingFilter implements LoggingFilter {
    private final LogPrinter logPrinter;
    private final IgnoreLoggingPath ignoreLoggingPath;
    private final boolean enableLoggingRequestBody;
    private final boolean enableLoggingResponseBody;
    private final int order;

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    @Nonnull
    public Mono<Void> filter(ServerWebExchange exchange, @Nonnull WebFilterChain chain) {
        if (isIgnoreLogging(exchange.getRequest()))
            return chain.filter(exchange);

        LoggingWebExchange webExchange = new LoggingWebExchange(exchange, logPrinter, enableLoggingRequestBody, enableLoggingResponseBody);
        return webExchange
                .enableLogging()
                .flatMap(it -> chain.filter(it).onErrorResume(webExchange::enableResponseLoggingWhenError));
    }

    private boolean isIgnoreLogging(ServerHttpRequest request) {
        return ignoreLoggingPath.isMatch(request.getMethod(), request.getPath().value());
    }
}
