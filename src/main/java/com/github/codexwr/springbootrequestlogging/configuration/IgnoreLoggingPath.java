package com.github.codexwr.springbootrequestlogging.configuration;

import jakarta.annotation.Nonnull;
import org.springframework.http.HttpMethod;

public interface IgnoreLoggingPath {
    boolean isMatch(@Nonnull HttpMethod method, @Nonnull String path);
}
