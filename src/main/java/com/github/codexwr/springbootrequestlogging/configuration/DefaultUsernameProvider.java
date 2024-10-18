package com.github.codexwr.springbootrequestlogging.configuration;

import com.github.codexwr.springbootrequestlogging.component.UsernameProvider;
import jakarta.annotation.Nullable;

class DefaultUsernameProvider implements UsernameProvider {


    @Nullable
    @Override
    public String getUsername() {
        return null;
    }
}
