package com.github.codexwr.springbootrequestlogging.legacy;

import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface PayloadMessageReader {
    @Nullable
    String getRequestPayloadMessage(HttpServletRequest request);

    @Nullable
    String getResponsePayloadMessage(HttpServletResponse response);
}
