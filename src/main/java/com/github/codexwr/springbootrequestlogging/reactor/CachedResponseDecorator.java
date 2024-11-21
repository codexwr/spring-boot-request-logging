package com.github.codexwr.springbootrequestlogging.reactor;

import jakarta.annotation.Nonnull;
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;

class CachedResponseDecorator extends ServerHttpResponseDecorator implements LoggingDecorator {
    private byte[] cachedBody = null;

    public CachedResponseDecorator(ServerHttpResponse delegate) {
        super(delegate);
    }

    @Override
    @Nonnull
    public Mono<Void> writeWith(@Nonnull Publisher<? extends DataBuffer> body) {
        if (!isCachedBody())
            return super.writeWith(body);

        var publisher = Flux.from(body)
                .doOnNext(dataBuffer -> {
                    cachedBody = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.toByteBuffer(ByteBuffer.wrap(cachedBody));
                });

        return super.writeWith(publisher);
    }

    private boolean isCachedBody() {
        return isCompatibleMediaType(MediaType.APPLICATION_JSON);
    }

    public String getCachedContentString() {
        if (cachedBody == null) return null;
        return new String(cachedBody, getCharset());
    }
}
