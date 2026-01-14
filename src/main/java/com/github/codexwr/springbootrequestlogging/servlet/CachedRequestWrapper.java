package com.github.codexwr.springbootrequestlogging.servlet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nullable;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;

import java.io.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

class CachedRequestWrapper extends HttpServletRequestWrapper implements LoggingWrapper {
    private static final Logger log = LoggerFactory.getLogger(CachedRequestWrapper.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private volatile byte[] cachedData;

    public CachedRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (!isCachedBody())
            return super.getInputStream();

        return getCachedInputStream();
    }

    private CachedInputStream getCachedInputStream() throws IOException {
        if (cachedData == null) {
            synchronized (this) {
                if (cachedData == null)
                    cachedData = super.getInputStream().readAllBytes();
            }
        }

        return new CachedInputStream(cachedData);
    }

    @Override
    public BufferedReader getReader() throws IOException {
        if (!isCachedBody())
            return super.getReader();

        return new BufferedReader(new InputStreamReader(getInputStream()));
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isCachedBody() {
        return isCompatibleMediaType(MediaType.APPLICATION_JSON);
    }

    private byte[] getInputContent() {
        if (isCompatibleMediaType(MediaType.APPLICATION_FORM_URLENCODED)) {
            return getFormDataContent();
        }

        if (isCompatibleMediaType(MediaType.MULTIPART_FORM_DATA)) {
            return getMultipartContent();
        }

        return null;
    }

    private byte[] getFormDataContent() {
        var contents = CollectionUtils.<String, String>toMultiValueMap(new HashMap<>());

        getRequest()
                .getParameterMap()
                .forEach((key, value) -> contents.addAll(key, Arrays.stream(value).toList()));

        return serializedMultiValueMap(contents);
    }

    private byte[] getMultipartContent() {
        var ctx = WebApplicationContextUtils.getWebApplicationContext(getRequest().getServletContext());
        if (ctx == null) {
            log.trace("WebApplicationContext is null");
            return null;
        }

        var multipartResolver = ctx.getBean(MultipartResolver.class);
        var multipart = multipartResolver.resolveMultipart((HttpServletRequest) getRequest());
        var contents = extractMultipartData(multipart);

        return serializedMultiValueMap(contents);
    }

    private MultiValueMap<String, String> extractMultipartData(MultipartHttpServletRequest multipart) {
        var contents = CollectionUtils.<String, String>toMultiValueMap(new HashMap<>());

        multipart.getParameterMap()
                .forEach((key, value) -> contents.addAll(key, Arrays.stream(value).toList()));

        var files = multipart.getMultiFileMap().values().stream()
                .flatMap(Collection::stream)
                .toList();

        files.forEach(file -> contents.add(file.getName(), file.getOriginalFilename()));

        return contents;
    }

    @Nullable
    private byte[] serializedMultiValueMap(MultiValueMap<String, String> multiValueMap) {
        try {
            return objectMapper.writeValueAsString(multiValueMap).getBytes(getCharacterEncoding());
        } catch (JsonProcessingException | UnsupportedEncodingException e) {
            log.trace("Failed to serialize MultiValueMap", e);
            return null;
        }
    }

    public byte[] getCachedContentBody() {
        try {
            if (!isCachedBody())
                return getInputContent();

            try(InputStream is = getCachedInputStream()) {
                return is.readAllBytes();
            }
        } catch (Exception e) {
            log.trace("Failed to get cached request content body", e);
            return null;
        }
    }

    public String getCachedContentString() {
        var bodyBuffer = getCachedContentBody();
        if (bodyBuffer == null) return null;

        return new String(bodyBuffer, getCharset(getCharacterEncoding()));
    }
}
