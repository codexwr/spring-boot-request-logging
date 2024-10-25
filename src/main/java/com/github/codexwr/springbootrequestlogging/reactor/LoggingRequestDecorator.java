package com.github.codexwr.springbootrequestlogging.reactor;

import com.github.codexwr.springbootrequestlogging.configuration.LogPrinter;
import lombok.Getter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.function.Supplier;

class LoggingRequestDecorator extends ServerHttpRequestDecorator implements LoggingDecorator {
    private final Supplier<ServerWebExchange> exchangeSupplier;
    private final LogPrinter logPrinter;

    @Getter
    private Long executionTime = System.currentTimeMillis();

    public LoggingRequestDecorator(ServerHttpRequest delegate, Supplier<ServerWebExchange> exchangeSupplier, LogPrinter logPrinter) {
        super(delegate);

        this.exchangeSupplier = exchangeSupplier;
        this.logPrinter = logPrinter;
    }

    public Mono<Void> logPrint() {
        executionTime = System.currentTimeMillis();
        var exchange = exchangeSupplier.get();

        return Mono.zip(exchange.getSession(), getRequestBody())
                .doOnNext(sessionAndBody -> logPrinter.request(getMethod(),
                        getURI().getPath(),
                        getURI().getQuery(),
                        getRemoteAddress() != null ? getRemoteAddress().getHostString() : null,
                        sessionAndBody.getT1().isStarted() ? sessionAndBody.getT1().getId() : null,
                        getHeaders(),
                        getContentType(),
                        sessionAndBody.getT2().orElse(null))
                ).then();
    }

    public Mono<Optional<String>> getRequestBody() {
        Mono<Optional<String>> content = Mono.just(Optional.empty());

        var delegate = getNativeRequest(getDelegate(), CachedRequestDecorator.class);
        if (delegate != null) {
            content = delegate.getCachedContentString()
                    .map(Optional::of)
                    .defaultIfEmpty(Optional.empty());
        }

        return content;
    }
}
