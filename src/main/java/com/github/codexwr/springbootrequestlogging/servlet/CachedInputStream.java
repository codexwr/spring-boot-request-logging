package com.github.codexwr.springbootrequestlogging.servlet;

import jakarta.annotation.Nonnull;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;

class CachedInputStream extends ServletInputStream {
    private final ByteArrayInputStream buffer;
    private boolean isFinished = false;

    public CachedInputStream(byte[] data) {
        this.buffer = new ByteArrayInputStream(data);
    }

    @Override
    public boolean isFinished() {
        return isFinished;
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
        var count = buffer.read();
        if (count == -1) isFinished = true;
        return count;
    }

    @Override
    public int read(@Nonnull byte[] b, int off, int len) {
        var count = buffer.read(b, off, len);
        if (count == -1) isFinished = true;
        return count;
    }
}
