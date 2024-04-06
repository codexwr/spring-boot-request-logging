package com.github.codexwr.springbootrequestlogging;

import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface PayloadMessageReader {
    @Nullable
    String getRequestPayloadMessage(HttpServletRequest request);

    @Nullable
    String getResponsePayloadMessage(HttpServletResponse response);
}
