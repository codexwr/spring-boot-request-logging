package com.github.codexwr.springbootrequestlogging.servlet;

import jakarta.servlet.Filter;
import org.springframework.core.Ordered;

public interface LoggingFilter extends Filter, Ordered {
}
