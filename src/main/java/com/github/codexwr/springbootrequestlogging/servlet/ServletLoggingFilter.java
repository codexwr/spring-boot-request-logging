package com.github.codexwr.springbootrequestlogging.servlet;

import com.github.codexwr.springbootrequestlogging.component.IgnoreLoggingPath;
import com.github.codexwr.springbootrequestlogging.component.LogPrinter;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import java.io.IOException;

@RequiredArgsConstructor
public class ServletLoggingFilter extends OncePerRequestFilter implements Ordered {
    private final LogPrinter logPrinter;
    private final IgnoreLoggingPath ignoreLoggingPath;
    private final boolean enableLoggingRequestBody;
    private final boolean enableLoggingResponseBody;
    private final int order;

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull FilterChain filterChain) throws ServletException, IOException {
        final var isFirstRequest = !isAsyncStarted(request);

        if (!isFirstRequest || isIgnoreLogging(request)) {
            next(request, response, filterChain);
            return;
        }

        var requestToUse = ensureLoggingRequest(request);
        var responseToUse = ensureLoggingResponse(response, requestToUse);

        requestToUse.logPrint();
        next(requestToUse, responseToUse, filterChain);
    }

    private void next(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        try {
            filterChain.doFilter(request, response);
        } finally {
            completeWithResponse(request, response);
        }
    }

    private void completeWithResponse(HttpServletRequest request, HttpServletResponse response) {
        if (isAsyncStarted(request)) return;

        var responseWrapper = WebUtils.getNativeResponse(response, LoggingResponseWrapper.class);
        if (responseWrapper == null) return;

        responseWrapper.logPrint();
    }


    private boolean isIgnoreLogging(HttpServletRequest request) {
        return ignoreLoggingPath.isMatch(HttpMethod.valueOf(request.getMethod()), request.getServletPath());
    }

    private LoggingRequestWrapper ensureLoggingRequest(HttpServletRequest request) {
        if (request instanceof LoggingRequestWrapper) return (LoggingRequestWrapper) request;

        return new LoggingRequestWrapper(enableLoggingRequestBody ? new CachedRequestWrapper(request) : request, logPrinter);
    }

    private LoggingResponseWrapper ensureLoggingResponse(HttpServletResponse response, LoggingRequestWrapper request) {
        return new LoggingResponseWrapper(enableLoggingResponseBody ? new CachedResponseWrapper(response) : response, request, logPrinter);
    }
}
