package com.github.codexwr.springbootrequestlogging.reactor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.function.Supplier;

class CachedRequestDecorator extends ServerHttpRequestDecorator implements LoggingDecorator {
    private static final Logger log = LoggerFactory.getLogger(CachedRequestDecorator.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final Supplier<ServerWebExchange> exchangeSupplier;
    private byte[] cachedBody = null;

    public CachedRequestDecorator(ServerHttpRequest delegate, Supplier<ServerWebExchange> exchangeSupplier) {
        super(delegate);
        this.exchangeSupplier = exchangeSupplier;
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

    private Mono<byte[]> getInputContent() {
        if (isCompatibleMediaType(MediaType.APPLICATION_FORM_URLENCODED))
            return getFormDataContent();

        if (isCompatibleMediaType(MediaType.MULTIPART_FORM_DATA))
            return getMultipartDataContent();

        return Mono.empty();
    }

    private Mono<byte[]> getFormDataContent() {
        var exchange = exchangeSupplier.get();

        return exchange.getFormData()
                .mapNotNull(this::serializedMultiValueMap);
    }

    private Mono<byte[]> getMultipartDataContent() {
        var exchange = exchangeSupplier.get();

        return exchange.getMultipartData()
                .map(this::extractMultipartData)
                .mapNotNull(this::serializedMultiValueMap);
    }

    @Nullable
    private byte[] serializedMultiValueMap(MultiValueMap<String, String> multiValueMap) {
        try {
            return objectMapper.writeValueAsString(multiValueMap).getBytes(getCharset());
        } catch (JsonProcessingException e) {
            log.trace("Failed to serialize multiValueMap", e);
            return null;
        }
    }

    private MultiValueMap<String, String> extractMultipartData(MultiValueMap<String, Part> partMap) {
        var partList = partMap.values().stream()
                .flatMap(Collection::stream)
                .toList();

        var contents = CollectionUtils.<String, String>toMultiValueMap(new HashMap<>());
        partList.forEach(part -> {
            if (part instanceof FilePart filePart) {
                contents.add(filePart.name(), filePart.filename());
            } else if (part instanceof FormFieldPart formFieldPart) {
                contents.add(formFieldPart.name(), formFieldPart.value());
            }
        });

        return contents;
    }

    public Mono<byte[]> getCachedContentBody() {
        if (!isCachedBody()) return getInputContent();

        return Mono.justOrEmpty(cachedBody)
                .switchIfEmpty(storeCachedBody(super.getBody()));
    }

    public Mono<String> getCachedContentString() {
        return getCachedContentBody()
                .map(it -> new String(it, getCharset()));
    }
}
