package com.github.codexwr.springbootrequestlogging.reactor;

import com.github.codexwr.springbootrequestlogging.configuration.LogPrinter;
import jakarta.annotation.Nonnull;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebExchangeDecorator;
import reactor.core.publisher.Mono;

class LoggingWebExchange extends ServerWebExchangeDecorator {
    private final LoggingRequestDecorator loggingRequestDecorator;
    private final LoggingResponseDecorator loggingResponseDecorator;

    protected LoggingWebExchange(ServerWebExchange delegate, LogPrinter logPrinter, boolean isCachedRequest, boolean isCachedResponse) {
        super(delegate);

        // default
        var req = super.getRequest();
        var res = super.getResponse();

        // cached
        if (isCachedRequest) req = new CachedRequestDecorator(req, this::getDelegate);
        if (isCachedResponse) res = new CachedResponseDecorator(res);

        // logger
        loggingRequestDecorator = new LoggingRequestDecorator(req, this::getDelegate, logPrinter);
        loggingResponseDecorator = new LoggingResponseDecorator(res, this::getDelegate, loggingRequestDecorator, logPrinter);
    }

    @Override
    @Nonnull
    public ServerHttpRequest getRequest() {
        return loggingRequestDecorator;
    }

    @Override
    @Nonnull
    public ServerHttpResponse getResponse() {
        return loggingResponseDecorator;
    }

    public Mono<ServerWebExchange> enableLogging() {
        return Mono.defer(() -> loggingRequestDecorator.logPrint().then(Mono.just(this)));
    }

    public Mono<Void> enableResponseLoggingWhenError(Throwable error) {
        return Mono.defer(() -> {
                    if (error instanceof ErrorResponseException statusException)
                        loggingResponseDecorator.setStatusCode(statusException.getStatusCode());

                    return loggingResponseDecorator.logPrint();
                })
                .then(Mono.error(error));
    }
}
