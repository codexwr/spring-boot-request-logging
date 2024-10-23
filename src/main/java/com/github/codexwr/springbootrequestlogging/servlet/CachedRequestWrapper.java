package com.github.codexwr.springbootrequestlogging.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.http.MediaType;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class CachedRequestWrapper extends HttpServletRequestWrapper implements LoggingWrapper {
    private static final ObjectMapper objectMapper = new ObjectMapper();

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

    private byte[] getInputContent() throws Exception {
        if (isCompatibleMediaType(MediaType.APPLICATION_FORM_URLENCODED))
            return objectMapper.writeValueAsString(getRequest().getParameterMap()).getBytes(getCharacterEncoding());

        if (isCompatibleMediaType(MediaType.MULTIPART_FORM_DATA)) {
            WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(getRequest().getServletContext());
            assert context != null;
            MultipartResolver multipartResolver = context.getBean(MultipartResolver.class);
            MultipartHttpServletRequest multipart = multipartResolver.resolveMultipart((HttpServletRequest) getRequest());
            Map<String, Object> content = new HashMap<>(multipart.getParameterMap().size() + 1);
            content.putAll(multipart.getParameterMap());
            multipart.getMultiFileMap().forEach((key, value) -> {
                @SuppressWarnings("unchecked")
                var fileNames = (LinkedHashMap<String, List<String>>) content.computeIfAbsent("fileNames", k -> new LinkedHashMap<>());
                fileNames.put(key, value.stream().map(MultipartFile::getOriginalFilename).toList());
            });

            return objectMapper.writeValueAsString(content).getBytes(getCharacterEncoding());
        }

        return null;
    }


    public byte[] getCachedContentBody() {
        try {
            if (!isCachedBody())
                return getInputContent();

            return getCachedInputStream().getCachedBuffer();
        } catch (Exception e) {
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
