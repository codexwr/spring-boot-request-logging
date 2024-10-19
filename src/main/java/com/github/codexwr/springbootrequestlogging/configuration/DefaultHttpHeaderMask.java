package com.github.codexwr.springbootrequestlogging.configuration;

import com.github.codexwr.springbootrequestlogging.component.HttpHeaderMask;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

class DefaultHttpHeaderMask implements HttpHeaderMask {
    private static final AntPathMatcher pathMatcher = new AntPathMatcher();

    // <Method, <path pattern, maskKey>>
    private final String maskOverlay;
    private final Set<String> defaultMaks;
    private final Map<HttpMethod, Map<String, Set<String>>> patternMasks = new HashMap<>();

    public DefaultHttpHeaderMask(String maskOverlay, @Nullable Set<String> defaultMasks, @Nullable Collection<LoggingFilterProperties.PathKeyMask> pathKeyMasks) {
        this.maskOverlay = maskOverlay;

        if (CollectionUtils.isEmpty(defaultMasks))
            this.defaultMaks = Collections.emptySet();
        else
            this.defaultMaks = defaultMasks.stream()
                    .map(String::toLowerCase)
                    .collect(Collectors.toUnmodifiableSet());

        if (CollectionUtils.isEmpty(pathKeyMasks)) return;
        for (LoggingFilterProperties.PathKeyMask pathKeyMask : pathKeyMasks) {
            if (CollectionUtils.isEmpty(pathKeyMask.getMaskKey())) continue;

            patternMasks.computeIfAbsent(pathKeyMask.getMethod(), k -> new HashMap<>())
                    .computeIfAbsent(pathKeyMask.getPathPattern(), k -> new HashSet<>())
                    .addAll(pathKeyMask.getMaskKey().stream().map(String::toLowerCase).toList());
        }
    }

    @Override
    public HttpHeaders getMaskingHeaders(@Nonnull HttpMethod method, @Nonnull String path, @Nonnull HttpHeaders headers) {
        Assert.notNull(method, "method must not be null");
        Assert.notNull(path, "path must not be null");
        Assert.notNull(headers, "headers must not be null");

        var patterns = patternMasks.get(method);
        if (CollectionUtils.isEmpty(patterns))
            return generateHeaderMask(headers, defaultMaks);

        var pattern = patterns.entrySet().stream()
                .filter(entry -> pathMatcher.match(entry.getKey(), path))
                .findFirst()
                .orElse(null);

        if (pattern == null)
            return generateHeaderMask(headers, defaultMaks);

        return generateHeaderMask(headers, pattern.getValue());
    }

    @SuppressWarnings("DataFlowIssue")
    private HttpHeaders generateHeaderMask(HttpHeaders headers, Set<String> maskKeys) {
        if (CollectionUtils.isEmpty(headers) || CollectionUtils.isEmpty(maskKeys)) return headers;

        var maskHeaders = new HttpHeaders();
        for (String key : headers.keySet()) {
            if (maskKeys.contains(key.toLowerCase())) maskHeaders.put(key, List.of(maskOverlay));
            else maskHeaders.put(key, headers.get(key));
        }

        return maskHeaders;
    }
}
