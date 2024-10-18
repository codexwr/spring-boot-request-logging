package com.github.codexwr.springbootrequestlogging.configuration;

import com.github.codexwr.springbootrequestlogging.component.IgnoreLoggingPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DefaultIgnoreLoggingPathTest {
    public static class IgnoreLoggingPathSteps {
        public static final Set<String> ignoredPathPatterns = Set.of("/open/**", "/rest-doc/api.json", "/auth/**");

        public static final Set<LoggingFilterProperties.ExcludeLoggingPath> ignoredPaths = Set.of(
                new LoggingFilterProperties.ExcludeLoggingPath(HttpMethod.GET.name(), ignoredPathPatterns)
        );
        public static final IgnoreLoggingPath defaultIgnoreLoggingPath = new DefaultIgnoreLoggingPath(ignoredPaths);
    }

    @Test
    @DisplayName("경로 불일치1")
    void noMatchPath1() {
        // given
        var method = HttpMethod.GET;
        var path = "/rest-doc/script.js";

        // when
        var isMatch = IgnoreLoggingPathSteps.defaultIgnoreLoggingPath.isMatch(method, path);

        // then
        assertFalse(isMatch);
    }

    @Test
    @DisplayName("경로 불일치2")
    void noMatchPath2() {
        // given
        var method = HttpMethod.POST;
        var path = "/rest-doc/api.json";

        // when
        var isMatch = IgnoreLoggingPathSteps.defaultIgnoreLoggingPath.isMatch(method, path);

        // then
        assertFalse(isMatch);
    }

    @Test
    @DisplayName("경로 일치1")
    void matchPath1() {
        // given
        var method = HttpMethod.GET;
        var path = "/rest-doc/api.json";

        // when
        var isMatch = IgnoreLoggingPathSteps.defaultIgnoreLoggingPath.isMatch(method, path);

        // then
        assertTrue(isMatch);
    }

    @Test
    @DisplayName("경로 일치2")
    void matchPath2() {
        // given
        var method = HttpMethod.GET;
        var path = "/open/language/2";

        // when
        var isMatch = IgnoreLoggingPathSteps.defaultIgnoreLoggingPath.isMatch(method, path);

        // then
        assertTrue(isMatch);
    }
}