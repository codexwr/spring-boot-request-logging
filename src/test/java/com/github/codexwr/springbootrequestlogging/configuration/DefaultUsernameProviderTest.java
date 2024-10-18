package com.github.codexwr.springbootrequestlogging.configuration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;

import static org.junit.jupiter.api.Assertions.*;

class DefaultUsernameProviderTest {
    @Test
    @DisplayName("null check")
    void getUsername() {
        // given
        var provider = new DefaultUsernameProvider();

        // when && then
        assertFalse(StringUtils.isNotBlank(provider.getUsername()));
    }
}