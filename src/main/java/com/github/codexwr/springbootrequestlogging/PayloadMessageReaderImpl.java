package com.github.codexwr.springbootrequestlogging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.WebUtils;

import java.nio.charset.Charset;
import java.util.HashMap;

@RequiredArgsConstructor
class PayloadMessageReaderImpl implements PayloadMessageReader {
    private final ObjectMapper objectMapper;

    @Nullable
    @Override
    public String getRequestPayloadMessage(HttpServletRequest request) {
        var isMultipart = StringUtils.hasText(request.getContentType()) && request.getContentType().contains(MediaType.MULTIPART_FORM_DATA_VALUE);

        if (isMultipart) {
            return getMultipartMessagePayload(request);
        } else {
            return getRequestPayload(request);
        }
    }

    @Nullable
    @Override
    public String getResponsePayloadMessage(HttpServletResponse response) {
        return getResponsePayload(response);
    }

    @Nullable
    private String getMultipartMessagePayload(HttpServletRequest request) {
        var context = WebApplicationContextUtils.getWebApplicationContext(request.getServletContext());
        if (context == null) return "[unknown]";

        var multipartResolver = context.getBean(MultipartResolver.class);

        var multipart = multipartResolver.resolveMultipart(request);
        var payload = new HashMap<String, Object>(multipart.getParameterMap());
        payload.put("fileNames", multipart.getFileNames());

        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ignore) {
            return "[unknown]";
        }
    }

    @Nullable
    private String getRequestPayload(HttpServletRequest request) {
        var wrapper = WebUtils.getNativeRequest(request, ContentCachingRequestWrapper.class);
        if (wrapper == null) return null;

        return wrapper.getContentAsString();
    }

    @Nullable
    private String getResponsePayload(HttpServletResponse response) {
        var wrapper = WebUtils.getNativeResponse(response, ContentCachingResponseWrapper.class);
        if (wrapper == null) return null;

        try {
            return new String(wrapper.getContentAsByteArray(), Charset.forName(wrapper.getCharacterEncoding()));
        } catch (Exception ignore) {
            return "[unknown]";
        }
    }
}
