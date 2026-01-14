package com.github.codexwr.springbootrequestlogging.servlet;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;

class CachedInputStream extends ServletInputStream {
    private final ByteArrayInputStream buffer;

    public CachedInputStream(byte[] data) {
        this.buffer = new ByteArrayInputStream(data);
    }

    @Override
    public boolean isFinished() {
        return buffer.available() == 0;
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void close() throws IOException {
        buffer.close();
        super.close();
    }

    @Override
    public void setReadListener(ReadListener listener) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int read() {
        return buffer.read();
    }
}
