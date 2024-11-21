package com.github.codexwr.springbootrequestlogging.configuration;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;

import java.util.*;

class CommonBodyMask {
    private static final Logger log = LoggerFactory.getLogger(CommonBodyMask.class);
    protected static final AntPathMatcher pathMatcher = new AntPathMatcher();

    protected void initJsonPattern(Collection<LoggingFilterProperties.PathJsonMask> sources, Map<HttpMethod, Map<String, Set<String>>> targets) {
        if (CollectionUtils.isEmpty(sources)) return;

        sources.forEach(src -> {
            if (CollectionUtils.isEmpty(src.getMaskJson())) return;

            targets.computeIfAbsent(src.getMethod(), k -> new HashMap<>())
                    .computeIfAbsent(src.getPathPattern(), k -> new HashSet<>())
                    .addAll(src.getMaskJson());
        });
    }

    protected void initKeyPattern(Collection<LoggingFilterProperties.PathKeyMask> sources, Map<HttpMethod, Map<String, Set<String>>> targets) {
        if (CollectionUtils.isEmpty(sources)) return;

        sources.forEach(src -> {
            if (CollectionUtils.isEmpty(src.getMaskKey())) return;

            targets.computeIfAbsent(src.getMethod(), k -> new HashMap<>())
                    .computeIfAbsent(src.getPathPattern(), k -> new HashSet<>())
                    .addAll(src.getMaskKey());
        });
    }

    protected String getJsonMaksString(String jsonString, Set<String> masks, String maskString) {

        DocumentContext doc;
        try {
            doc = JsonPath.parse(jsonString);
        } catch (Exception ignore) {
            return jsonString;
        }

        masks.forEach(jsonPath -> {
            try {
                doc.set(jsonPath, maskString);
            } catch (Exception e) {
                log.trace("JsonPath masking error: {}", jsonPath, e);
            }
        });

        return doc.jsonString();
    }

    protected Set<String> getMaskPattern(String path, Map<String, Set<String>> patterns) {
        if (CollectionUtils.isEmpty(patterns)) return null;

        return patterns.entrySet().stream()
                .filter(entry -> pathMatcher.match(entry.getKey(), path))
                .findFirst()
                .map(Map.Entry::getValue)
                .orElse(null);
    }
}
