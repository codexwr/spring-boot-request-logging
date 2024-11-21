package com.github.codexwr.springbootrequestlogging.servlet;

import java.io.OutputStream;
import java.io.PrintWriter;

class PrintWriterWrapper extends PrintWriter {
    public PrintWriterWrapper(OutputStream out) {
        super(out);
    }
}
