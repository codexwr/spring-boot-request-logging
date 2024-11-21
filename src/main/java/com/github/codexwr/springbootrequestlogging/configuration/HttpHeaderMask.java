package com.github.codexwr.springbootrequestlogging.configuration;

import jakarta.annotation.Nonnull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

public interface HttpHeaderMask {
    HttpHeaders getMaskingHeaders(@Nonnull HttpMethod method, @Nonnull String path, @Nonnull HttpHeaders headers);
}
