package com.github.codexwr.springbootrequestlogging.component;


import jakarta.annotation.Nullable;

public interface UsernameProvider {
    @Nullable
    String getUsername();
}
