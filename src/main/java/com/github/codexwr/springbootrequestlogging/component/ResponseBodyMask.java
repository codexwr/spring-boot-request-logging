package com.github.codexwr.springbootrequestlogging.component;

import jakarta.annotation.Nonnull;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

public interface ResponseBodyMask {
    String getMaskBody(@Nonnull HttpMethod method, @Nonnull String path, @Nonnull MediaType contentType, @Nonnull String responseBody);
}
