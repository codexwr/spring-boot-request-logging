package com.github.codexwr.springbootrequestlogging.reactor;

import org.springframework.core.Ordered;
import org.springframework.web.server.WebFilter;

public interface LoggingFilter extends WebFilter, Ordered {
}
