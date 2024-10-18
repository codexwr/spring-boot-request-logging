package com.github.codexwr.springbootrequestlogging.configuration;

import com.github.codexwr.springbootrequestlogging.component.IgnoreLoggingPath;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.springframework.http.HttpMethod;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.*;

class DefaultIgnoreLoggingPath implements IgnoreLoggingPath {
    private static final AntPathMatcher pathMatcher = new AntPathMatcher();

    private final Map<HttpMethod, Set<String>> ignoredPatterns = new HashMap<>();

    public DefaultIgnoreLoggingPath(@Nullable  Collection<LoggingFilterProperties.ExcludeLoggingPath> excludes) {
        if (CollectionUtils.isEmpty(excludes)) return;

        for (LoggingFilterProperties.ExcludeLoggingPath exclude : excludes) {
            if (CollectionUtils.isEmpty(exclude.getPathPatterns()))
                continue;

            ignoredPatterns.computeIfAbsent(exclude.getMethod(), k -> new HashSet<>())
                    .addAll(exclude.getPathPatterns());
        }
    }

    @Override
    public boolean isMatch(@Nonnull HttpMethod method, @Nonnull String path) {
        Assert.notNull(method, "method must not be null");
        Assert.notNull(path, "path must not be null");

        var ignorePatterns =  ignoredPatterns.get(method);
        if (CollectionUtils.isEmpty(ignorePatterns)) return false;

        return ignorePatterns.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }
}
