package com.github.codexwr.springbootrequestlogging.servlet;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import org.springframework.util.FastByteArrayOutputStream;

import java.io.IOException;

class CachedOutputStream extends ServletOutputStream {
    private final FastByteArrayOutputStream cachedBuffer = new FastByteArrayOutputStream(1024);
    private final ServletOutputStream outputStream;

    public CachedOutputStream(ServletOutputStream outputStream) {
        this.outputStream = outputStream;
    }

    @Override
    public boolean isReady() {
        return outputStream.isReady();
    }

    @Override
    public void setWriteListener(WriteListener listener) {
        outputStream.setWriteListener(listener);
    }

    @Override
    public void write(int b) throws IOException {
        outputStream.write(b);
        cachedBuffer.write(b);
    }

    @Override
    public void close() throws IOException {
        super.close();

        outputStream.close();
        cachedBuffer.close();
    }

    public byte[] getCachedBuffer() {
        return cachedBuffer.toByteArray();
    }
}
