package com.github.codexwr.springbootrequestlogging.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.util.WebUtils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

interface LoggingWrapper {
    Logger log = LoggerFactory.getLogger(LoggingWrapper.class);

    default boolean isCompatibleMediaType(MediaType mediaType) {
        var contentType = getContentType(getContentType());
        return contentType != null && contentType.isCompatibleWith(mediaType);
    }

    default MediaType getContentType(String contentType) {
        try {
            return MediaType.valueOf(contentType);
        } catch (Exception e) {
            log.trace("contentType parsing error.", e);
            return null;
        }
    }

    default Charset getCharset(String encodeCharset) {
        Charset charset = null;
        try {
            charset = Charset.forName(encodeCharset);
        } catch (Exception e) {
            log.trace("charset parsing error.", e);
        }

        return charset != null ? charset : StandardCharsets.UTF_8;
    }

    String getContentType();
}
