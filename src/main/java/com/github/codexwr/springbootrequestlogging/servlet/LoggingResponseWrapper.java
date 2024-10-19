package com.github.codexwr.springbootrequestlogging.servlet;

import com.github.codexwr.springbootrequestlogging.component.LogPrinter;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.util.WebUtils;

class LoggingResponseWrapper extends HttpServletResponseWrapper implements LoggingWrapper {

    private final LoggingRequestWrapper loggingRequestWrapper;
    private final LogPrinter logPrinter;

    public LoggingResponseWrapper(HttpServletResponse response, LoggingRequestWrapper loggingRequestWrapper, LogPrinter logPrinter) {
        super(response);

        this.loggingRequestWrapper = loggingRequestWrapper;
        this.logPrinter = logPrinter;
    }

    public void logPrint() {
        logPrinter.response(executionTime(),
                HttpStatusCode.valueOf(getStatus()),
                loggingRequestWrapper.getHttpMethod(),
                loggingRequestWrapper.getRequestURI(),
                loggingRequestWrapper.getQueryString(),
                getContentType(loggingRequestWrapper.getContentType()),
                loggingRequestWrapper.getRequestBody(),
                getContentType(getContentType()),
                getResponseBody()
        );
    }

    private Long executionTime() {
        return System.currentTimeMillis() - loggingRequestWrapper.getExecutionTime();
    }

    public String getResponseBody() {
        var response = WebUtils.getNativeResponse(getResponse(), CachedResponseWrapper.class);

        return response != null ? response.getCachedContentString() : null;
    }
}
