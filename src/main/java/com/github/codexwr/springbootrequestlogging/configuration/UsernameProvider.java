package com.github.codexwr.springbootrequestlogging.configuration;


import jakarta.annotation.Nullable;

public interface UsernameProvider {
    @Nullable
    String getUsername();
}
