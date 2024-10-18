package com.github.codexwr.springbootrequestlogging.configuration;

import jakarta.annotation.Nullable;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpMethod;

import java.util.List;
import java.util.Set;

@Data
@ConfigurationProperties(prefix = LoggingFilterProperties.PREFIX)
public class LoggingFilterProperties {
    static final String PREFIX = "codexwr.springboot.request-logging";
    static final String ENABLED = PREFIX + ".enabled";

    /**
     * Enable request logging of client
     */
    private boolean enabled = true;

    /**
     * Filter order of request logging filter.
     * Default value is SecurityProperties.DEFAULT_FILTER_ORDER - 1
     */
    private int filterOrder = SecurityProperties.DEFAULT_FILTER_ORDER - 1;

    /**
     * Url patterns to exclude from logging.
     * Url is tested using 'AntPathMatcher'.
     * <pre>
     *     exclude-logging-paths:
     *       - method: post
     *         path-pattern:
     *           - /api/v1/users
     *           - /api/v1/users/&#42;&#42;
     *       - method: get
     *         path-pattern:
     *           - /api-docs/&#42;&#42;
     * </pre>
     */

    @Nullable
    private List<ExcludeLoggingPath> excludeLoggingPaths = null;

    /**
     * Determines whether the query string should be included in the log.
     */
    private boolean includeQueryString = true;

    /**
     * Determines whether the client information should be included in the log.
     */
    private boolean includeClientInfo = true;

    /**
     * Determines whether the headers should be included in the log.
     */
    private boolean includeHeaders = false;

    /**
     * Masks the value corresponding to the name of specified Url.
     * <pre>
     *     path-header-masks:
     *      - method: post
     *        path-pattern: '/api/member'
     *        mask-key:
     *          - Authorization
     *          - Postman-Token
     *      - method: get
     *        path-pattern: '/api/member/1'
     *        mask-key:
     *          - Authorization
     *          - Postman-Token
     * </pre>
     */
    @Nullable
    private List<PathKeyMask> pathHeaderMask = null;

    /**
     * This works if nothing match <b>'pathMaskingHeaders'</b>
     */
    @Nullable
    private Set<String> defaultHeaderMasks = null;

    /**
     * Determines whether the request json body should be included in the log.
     */
    private boolean includeRequestBody = false;

    /**
     * If the value of the JSON body of the requested URL matches the pattern, masking is applied.
     * <p>
     * The format of the pattern is JsonPath.
     * <pre>
     *     request-json-body-masks:
     *      - method: post
     *        path-pattern: '/api/auth/sign-up'
     *        mask-json:
     *          - '$.password'
     *          - '$.password.&#42;'
     *      - method: get
     *        path-pattern: '/api/auth/sign-in'
     *        mask-json:
     *          - '$.password'
     * </pre>
     */
    @Nullable
    private List<PathJsonMask> requestJsonBodyMasks = null;

    /**
     * Masks the value corresponding to the name of specified Url.
     * <pre>
     *     request-form-data-masks:
     *      - method: post
     *        path-pattern: '/api/member'
     *        mask-key:
     *          - phone
     *          - name
     * </pre>
     */
    @Nullable
    private List<PathKeyMask> requestFormDataMasks = null;

    /**
     * Determines whether the response payload should be included in the log.
     */
    private boolean includeResponseBody = false;

    /**
     * If the value of the JSON body of the response URL matches the pattern, masking is applied.
     * <p>
     * reference: {@link #requestJsonBodyMasks}
     */
    @Nullable
    private List<PathJsonMask> responseJsonBodyMasks = null;

    /**
     * Text output by masking
     */
    private String maskString = "{{***}}";


    /**
     * The indication of the beginning of a log
     */
    private String enterPrefixDecor = "[+] ";

    /**
     * The indication of the end of a log
     */
    private String exitPrefixDecor = "[-] ";

    @Data
    @NoArgsConstructor
    public static class ExcludeLoggingPath {
        private HttpMethod method = HttpMethod.valueOf("NONE");
        private Set<String> pathPatterns = Set.of();

        public ExcludeLoggingPath(String method, Set<String> pathPatterns) {
            setMethod(method);
            this.pathPatterns = pathPatterns;
        }

        public void setMethod(String method) {
            this.method = HttpMethod.valueOf(method.toUpperCase());
        }
    }

    @Data
    @NoArgsConstructor
    public static class PathJsonMask {
        private HttpMethod method = HttpMethod.valueOf("NONE");
        private String pathPattern = "";
        private Set<String> maskJson = Set.of();

        public PathJsonMask(String method, String pathPattern, Set<String> maskJson) {
            setMethod(method);
            this.pathPattern = pathPattern;
            this.maskJson = maskJson;
        }

        public void setMethod(String method) {
            this.method = HttpMethod.valueOf(method.toUpperCase());
        }
    }

    @Data
    @NoArgsConstructor
    public static class PathKeyMask {
        private HttpMethod method = HttpMethod.valueOf("NONE");
        private String pathPattern = "";
        private Set<String> maskKey = Set.of();

        public PathKeyMask(String method, String pathPattern, Set<String> maskKey) {
            setMethod(method);
            this.pathPattern = pathPattern;
            this.maskKey = maskKey;
        }

        public void setMethod(String method) {
            this.method = HttpMethod.valueOf(method.toUpperCase());
        }
    }
}
