package com.github.codexwr.springbootrequestlogging.reactor;

import jakarta.annotation.Nonnull;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;

class CachedRequestDecorator extends ServerHttpRequestDecorator implements LoggingDecorator {
    private byte[] cachedBody = null;

    public CachedRequestDecorator(ServerHttpRequest delegate) {
        super(delegate);
    }

    @Override
    @Nonnull
    public Flux<DataBuffer> getBody() {
        return Mono.justOrEmpty(cachedBody) // 1. 캐쉬 사용
                .switchIfEmpty(getCachedContentBody()) // 2. 캐쉬가 없으면 캐쉬 처리
                .map(DefaultDataBufferFactory.sharedInstance::wrap) // 3. DataBuffer 생성 및 body 전달
                .cast(DataBuffer.class)
                .flux()
                .switchIfEmpty(super.getBody()); // 4. [2]에서 캐쉬 되지 않으면 raw body 사용
    }

    private boolean isCachedBody() {
        return isCompatibleMediaType(MediaType.APPLICATION_JSON);
    }

    private Mono<byte[]> storeCachedBody(Flux<DataBuffer> dataBufferPublisher) {
        return DataBufferUtils.join(dataBufferPublisher)
                .map(it -> {
                    cachedBody = new byte[it.readableByteCount()];
                    it.toByteBuffer(ByteBuffer.wrap(cachedBody));
                    DataBufferUtils.release(it);
                    return cachedBody;
                });
    }

    public Mono<byte[]> getCachedContentBody() {
        if (!isCachedBody()) return Mono.empty();

        return Mono.justOrEmpty(cachedBody)
                .switchIfEmpty(storeCachedBody(super.getBody()));
    }

    public Mono<String> getCachedContentString() {
        return getCachedContentBody()
                .map(it -> new String(it, getCharset()));
    }
}
