package com.github.codexwr.springbootrequestlogging.reactor;

import com.github.codexwr.springbootrequestlogging.component.LogPrinter;
import com.github.codexwr.springbootrequestlogging.component.IgnoreLoggingPath;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class WebfluxLoggingFilter implements WebFilter, Ordered {
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
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        if (isIgnoreLogging(exchange.getRequest()))
            return chain.filter(exchange);

        return new LoggingWebExchange(exchange, logPrinter, enableLoggingRequestBody, enableLoggingResponseBody)
                .enableLogging()
                .flatMap(chain::filter);
    }

    private boolean isIgnoreLogging(ServerHttpRequest request) {
        return ignoreLoggingPath.isMatch(request.getMethod(), request.getPath().value());
    }
}
