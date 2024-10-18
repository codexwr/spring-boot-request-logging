package com.github.codexwr.springbootrequestlogging.configuration;

import com.github.codexwr.springbootrequestlogging.component.ResponseBodyMask;
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

class DefaultResponseBodyMask extends CommonBodyMask implements ResponseBodyMask {

    private final String maskOverlay;
    // <method, <path pattern, mask-json>>
    private final Map<HttpMethod, Map<String, Set<String>>> jsonMasks = new HashMap<>();

    public DefaultResponseBodyMask(String maskOverlay, @Nullable Collection<LoggingFilterProperties.PathJsonMask> jsonMasks) {
        this.maskOverlay = maskOverlay;

        initJsonPattern(jsonMasks, this.jsonMasks);
    }

    @Override
    public String getMaskBody(@Nonnull HttpMethod method, @Nonnull String path, @Nonnull MediaType contentType, @Nonnull String responseBody) {
        Assert.notNull(method, "method must not be null");
        Assert.notNull(path, "path must not be null");
        Assert.notNull(contentType, "contentType must not be null");
        Assert.notNull(responseBody, "responseBody must not be null");

        Assert.state(contentType.isCompatibleWith(MediaType.APPLICATION_JSON), "DefaultResponseBodyMask is not compatible with except MediaType.APPLICATION_JSON");

        var patterns = jsonMasks.get(method);
        var masks = getMaskPattern(path, patterns);
        if (CollectionUtils.isEmpty(masks)) return responseBody;

        return getJsonMaksString(responseBody, masks, maskOverlay);
    }
}
