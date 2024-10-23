package com.github.codexwr.springbootrequestlogging.configuration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.codexwr.springbootrequestlogging.component.RequestBodyMask;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

class DefaultRequestBodyMask extends CommonBodyMask implements RequestBodyMask {
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final String maskOverlay;
    // <method, <path pattern, mask-json>>
    private final Map<HttpMethod, Map<String, Set<String>>> jsonMasks = new HashMap<>();
    // <method, <path pattern, mask-key>>
    private final Map<HttpMethod, Map<String, Set<String>>> formMasks = new HashMap<>();

    public DefaultRequestBodyMask(String maskOverlay, @Nullable Collection<LoggingFilterProperties.PathJsonMask> jsonMasks, @Nullable Collection<LoggingFilterProperties.PathKeyMask> formMasks) {
        this.maskOverlay = maskOverlay;

        initJsonPattern(jsonMasks, this.jsonMasks);
        initKeyPattern(formMasks, this.formMasks);
    }

    @Override
    public String getMaskBody(@Nonnull HttpMethod method, @Nonnull String path, @Nonnull MediaType contentType, @Nonnull String requestBody) {
        Assert.notNull(method, "method must not be null");
        Assert.notNull(path, "path must not be null");
        Assert.notNull(contentType, "contentType must not be null");
        Assert.notNull(requestBody, "requestBody must not be null");

        if (contentType.isCompatibleWith(MediaType.APPLICATION_JSON))
            return generateJsonMask(method, path, requestBody);
        else if (contentType.isCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED) || contentType.isCompatibleWith(MediaType.MULTIPART_FORM_DATA))
            return generateFormMask(method, path, requestBody);
        else
            return requestBody;
    }

    public String generateJsonMask(@Nonnull HttpMethod method, @Nonnull String path, @Nonnull String requestBody) {
        var patterns = jsonMasks.get(method);
        var masks = getMaskPattern(path, patterns);
        if (CollectionUtils.isEmpty(masks)) {
            try {
                return objectMapper.readValue(requestBody, JsonNode.class).toString();
            } catch (JsonProcessingException e) {
                return requestBody;
            }
        }

        return getJsonMaksString(requestBody, masks, maskOverlay);
    }

    private String generateFormMask(HttpMethod method, String path, String requestBody) {
        var patterns = formMasks.get(method);
        var masks = getMaskPattern(path, patterns);
        if (CollectionUtils.isEmpty(masks)) return requestBody;

        try {
            var bodyObj = objectMapper.readValue(requestBody, new TypeReference<Map<String, Object>>() {
            });

            bodyObj.keySet().forEach(key -> {
                if (masks.contains(key)) bodyObj.put(key, maskOverlay);
            });

            return objectMapper.writeValueAsString(bodyObj);
        } catch (JsonProcessingException ignore) {
            return requestBody;
        }
    }
}
