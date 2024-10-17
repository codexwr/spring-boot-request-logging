package com.github.codexwr.springbootrequestlogging.legacy;

import jakarta.annotation.Nullable;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;


@Data
@ConfigurationProperties(prefix = RequestLoggingFilterProperties.REQUEST_LOGGING_FILTER)
public class RequestLoggingFilterProperties {
    static final String REQUEST_LOGGING_FILTER = "codexwr.springboot.request-logging.legacy";
    static final String REQUEST_LOGGING_FILTER_ENABLED = REQUEST_LOGGING_FILTER + ".enabled";

    /**
     * Enable request logging of client
     */
    private boolean enabled = true;

    /**
     * Filter order of request logging filter.
     * Default value is SecurityProperties.DEFAULT_FILTER_ORDER - 1
     */
    private int filterOrder = -101;

    /**
     * Url patterns to exclude from logging.
     * Url is tested using 'AntPathMatcher'.
     * <pre>
     * /api/v1/users, /api/v1/users/&#42;&#42;
     * </pre>
     */
    @Nullable
    private Set<String> excludeUrlPatterns = null;

    /**
     * The indication of the beginning of a log
     */
    private String enterPrefix = "[+] ";

    /**
     * The indication of the end of a log
     */
    private String exitPrefix = "[-] ";

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
     * Masks the value corresponding to the specified name.
     */
    @Nullable
    private Set<String> maskingHeaders = null;

    /**
     * Determines whether the request payload should be included in the log.
     */
    private boolean includeRequestPayload = false;

    /**
     * The maximum size of the request payload to be included in the log.
     */
    private int maxRequestPayloadSize = 64;

    /**
     * If the ContentType is application/json, the values corresponding to the specified pattern is masked.
     * The format of the pattern is JsonPath.
     * <pre>
     * $.password, $.password.&#42;
     * </pre>
     */
    @Nullable
    private Set<String> maskingRequestPayloadPattern = null;

    /**
     * Determines whether the response payload should be included in the log.
     */
    private boolean includeResponsePayload = false;

    /**
     * The maximum size of the response payload to be included in the log.
     */
    private int maxResponsePayloadSize = 64;

    /**
     * If the ContentType is application/json, the values corresponding to the specified pattern is masked.
     * The format of the pattern is JsonPath.
     * <pre>
     * $.password, $.password.&#42;
     * </pre>
     */
    @Nullable
    private Set<String> maskingResponsePayloadPattern = null;

    /**
     * Text output by masking
     */
    private String maskString = "{{***}}";
}
