package com.github.codexwr.springbootrequestlogging.servlet;

import com.github.codexwr.springbootrequestlogging.configuration.LogPrinter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import lombok.Getter;
import org.springframework.http.HttpMethod;
import org.springframework.web.util.WebUtils;

class LoggingRequestWrapper extends HttpServletRequestWrapper implements LoggingWrapper {
    private final LogPrinter logPrinter;

    @Getter
    private Long executionTime = System.currentTimeMillis();

    public LoggingRequestWrapper(HttpServletRequest request, LogPrinter logPrinter) {
        super(request);
        this.logPrinter = logPrinter;
    }

    public void logPrint() {
        executionTime = System.currentTimeMillis();

        logPrinter.request(getLogItem(this, null, null));
    }

    public HttpMethod getHttpMethod() {
        return HttpMethod.valueOf(getMethod());
    }

    public String getRequestBody() {
        var request = WebUtils.getNativeRequest(getRequest(), CachedRequestWrapper.class);

        return request != null ? request.getCachedContentString() : null;
    }

}
