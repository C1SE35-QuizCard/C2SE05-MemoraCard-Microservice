package com.microservices.demo.configuration;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.stereotype.Component;

@Component
public class CustomHeaderBearerTokenResolver implements BearerTokenResolver {
    @Override
    public String resolve(HttpServletRequest request) {
        return request.getHeader("X-Security-Token");
    }
}
