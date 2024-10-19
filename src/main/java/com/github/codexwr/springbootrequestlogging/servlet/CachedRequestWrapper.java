package com.github.codexwr.springbootrequestlogging.servlet;

import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.http.MediaType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

class CachedRequestWrapper extends HttpServletRequestWrapper implements LoggingWrapper {
    private CachedInputStream cachedInputStream = null;
    private BufferedReader bufferedReader = null;

    public CachedRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (!isCachedBody())
            return super.getInputStream();

        return getCachedInputStream();
    }

    @Override
    public BufferedReader getReader() throws IOException {
        if (!isCachedBody())
            return super.getReader();

        if (bufferedReader == null) {
            bufferedReader = new BufferedReader(new InputStreamReader(getInputStream()));
        }

        return bufferedReader;
    }

    private CachedInputStream getCachedInputStream() throws IOException {
        if (cachedInputStream != null)
            return cachedInputStream;

        cachedInputStream = new CachedInputStream(super.getInputStream());

        return cachedInputStream;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isCachedBody() {
        return isCompatibleMediaType(MediaType.APPLICATION_JSON);
    }

    public byte[] getCachedContentBody() {
        if (!isCachedBody())
            return null;

        try {
            return getCachedInputStream().getCachedBuffer();
        } catch (IOException e) {
            log.trace("Failed to get cached content body", e);
            return null;
        }
    }

    public String getCachedContentString() {
        var bodyBuffer = getCachedContentBody();
        if (bodyBuffer == null) return null;

        return new String(bodyBuffer, getCharset(getCharacterEncoding()));
    }
}
