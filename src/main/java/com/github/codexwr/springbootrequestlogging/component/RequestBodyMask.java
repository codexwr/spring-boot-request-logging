package com.github.codexwr.springbootrequestlogging.component;

import jakarta.annotation.Nonnull;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

public interface RequestBodyMask {
    String getMaskBody(@Nonnull HttpMethod method, @Nonnull String path, @Nonnull MediaType contentType, @Nonnull String requestBody);
}
