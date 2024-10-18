package com.github.codexwr.springbootrequestlogging.reactor;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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

    HttpHeaders getHeaders();
}
