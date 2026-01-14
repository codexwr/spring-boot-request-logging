package com.github.codexwr.springbootrequestlogging.servlet;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

class CachedResponseWrapper extends HttpServletResponseWrapper implements LoggingWrapper {
    private volatile CachedOutputStream cachedOutputStream = null;
    private volatile PrintWriterWrapper printWriterWrapper = null;
    private boolean outputStreamUsed = false;
    private boolean writerUsed = false;

    public CachedResponseWrapper(HttpServletResponse response) {
        super(response);
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (!isCachedBody())
            return super.getOutputStream();

        if (writerUsed) {
            throw new IllegalStateException("getWriter() has already been called.");
        }
        outputStreamUsed = true;
        return getCachedOutputStream();
    }

    private CachedOutputStream getCachedOutputStream() throws IOException {
        if (cachedOutputStream == null) {
            synchronized (this) {
                if (cachedOutputStream == null)
                    cachedOutputStream = new CachedOutputStream(super.getOutputStream());
            }
        }

        return cachedOutputStream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (!isCachedBody())
            return super.getWriter();

        if (outputStreamUsed) {
            throw new IllegalStateException("getOutputStream() has already been called.");
        }
        writerUsed = true;
        return getPrintWriterWrapper();
    }

    private PrintWriterWrapper getPrintWriterWrapper() throws IOException {
        if (printWriterWrapper == null) {
            synchronized (this) {
                if (printWriterWrapper == null)
                    printWriterWrapper = new PrintWriterWrapper(getCachedOutputStream());
            }
        }

        return printWriterWrapper;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isCachedBody() {
        return isCompatibleMediaType(MediaType.APPLICATION_JSON);
    }

    public byte[] getCachedContentBody() {
        if (!isCachedBody())
            return null;

        try (CachedOutputStream os = getCachedOutputStream()) {
            return os.getCachedBuffer();
        } catch (IOException e) {
            log.trace("Failed to get cached response content body", e);
            return null;
        }
    }

    public String getCachedContentString() {
        var bodyBuffer = getCachedContentBody();
        if (bodyBuffer == null) return null;

        return new String(bodyBuffer, getCharset(getCharacterEncoding()));
    }
}
