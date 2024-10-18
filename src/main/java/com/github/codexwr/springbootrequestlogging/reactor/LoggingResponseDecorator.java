package com.github.codexwr.springbootrequestlogging.reactor;

import com.github.codexwr.springbootrequestlogging.component.LogPrinter;
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.Objects;
import java.util.function.Supplier;

class LoggingResponseDecorator extends ServerHttpResponseDecorator {
    private final LoggingRequestDecorator loggingRequestDelegate;
    private final LogPrinter logPrinter;

    private final Sinks.Empty<Void> printComplete = Sinks.empty();

    public LoggingResponseDecorator(ServerHttpResponse delegate, LoggingRequestDecorator loggingRequestDelegate, LogPrinter logPrinter) {
        super(delegate);

        this.loggingRequestDelegate = loggingRequestDelegate;
        this.logPrinter = logPrinter;
    }

    @Override
    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
        return super.writeWith(body).then(logPrint());
    }

    @Override
    public Mono<Void> writeAndFlushWith(Publisher<? extends Publisher<? extends DataBuffer>> body) {
        return super.writeAndFlushWith(body).then(logPrint());
    }

    @Override
    public void beforeCommit(Supplier<? extends Mono<Void>> action) {
        super.beforeCommit(() -> action.get().then(logPrint()));
    }

    private Mono<Void> logPrint() {
        return Mono.just(true)
                .takeUntilOther(printComplete.asMono())
                .map(it -> printComplete.tryEmitEmpty())
                .flatMap(it -> loggingRequestDelegate.getRequestBody())
                .doOnNext(requestBody -> logPrinter.response(executionTime(),
                        Objects.requireNonNullElse(getStatusCode(), HttpStatus.VARIANT_ALSO_NEGOTIATES),
                        loggingRequestDelegate.getMethod(),
                        loggingRequestDelegate.getURI().getPath(),
                        loggingRequestDelegate.getURI().getQuery(),
                        loggingRequestDelegate.getHeaders().getContentType(),
                        requestBody.orElse(null),
                        getHeaders().getContentType(),
                        getResponseBody())
                ).then();
    }

    private String getResponseBody() {
        if (getDelegate() instanceof CachedResponseDecorator) {
            return ((CachedResponseDecorator) getDelegate()).getCachedContentString();
        }

        return null;
    }

    private Long executionTime() {
        return System.currentTimeMillis() - loggingRequestDelegate.getExecutionTime();
    }
}
