package com.github.codexwr.springbootrequestlogging.servlet;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.io.PrintWriter;

class CachedResponseWrapper extends HttpServletResponseWrapper implements LoggingWrapper {
    private CachedOutputStream cachedOutputStream = null;
    private PrintWriterWrapper printWriterWrapper = null;

    public CachedResponseWrapper(HttpServletResponse response) {
        super(response);
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (!isCachedBody())
            return super.getOutputStream();

        return getCachedOutputStream();
    }

    private ServletOutputStream getCachedOutputStream() throws IOException {
        if (cachedOutputStream != null)
            return cachedOutputStream;

        cachedOutputStream = new CachedOutputStream(super.getOutputStream());

        return cachedOutputStream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (!isCachedBody())
            return super.getWriter();

        return getPrintWriterWrapper();
    }

    private PrintWriterWrapper getPrintWriterWrapper() throws IOException {
        if (printWriterWrapper != null)
            return printWriterWrapper;

        printWriterWrapper = new PrintWriterWrapper(getCachedOutputStream());

        return printWriterWrapper;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isCachedBody() {
        return isCompatibleMediaType(MediaType.APPLICATION_JSON);
    }

    public byte[] getCachedContentBody() {
        if (!isCachedBody())
            return null;

        return cachedOutputStream.getCachedBuffer();
    }

    public String getCachedContentString() {
        var bodyBuffer = getCachedContentBody();
        if (bodyBuffer == null) return null;

        return new String(bodyBuffer, getCharset(getCharacterEncoding()));
    }
}
