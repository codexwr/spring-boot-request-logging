package com.github.codexwr.springbootrequestlogging.servlet;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import org.springframework.util.FastByteArrayOutputStream;

import java.io.IOException;
import java.io.InputStream;

class CachedInputStream extends ServletInputStream {
    private final FastByteArrayOutputStream cachedBuffer;
    private final InputStream inputStream;

    private boolean isFinished = false;

    public CachedInputStream(ServletInputStream inputStream) throws IOException {
        int initialCapacity = Math.max(512, inputStream.available());
        this.cachedBuffer = new FastByteArrayOutputStream(initialCapacity);
        cachedBuffer.write(inputStream.readAllBytes());
        this.inputStream = cachedBuffer.getInputStream();
    }

    @Override
    public boolean isFinished() {
        return isFinished;
    }

    @Override
    public boolean isReady() {
        return inputStream != null;
    }

    @Override
    public void close() throws IOException {
        super.close();

        if (inputStream != null) inputStream.close();
        if (cachedBuffer != null) cachedBuffer.close();
    }

    @Override
    public void setReadListener(ReadListener listener) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int read() throws IOException {
        var data = inputStream.read();
        if (data != -1) isFinished = true;
        return data;
    }

    public byte[] getCachedBuffer() {
        return cachedBuffer.toByteArray();
    }
}
