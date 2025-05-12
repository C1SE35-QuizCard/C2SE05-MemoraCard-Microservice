package com.example.quizcards.controller.security;


import com.example.quizcards.helpers.AspectHelpers.authorize.PreAuthorizeMappingAspect;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Map;
import java.util.stream.Collectors;

@Component
@Endpoint(id = "secured-mappings")
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class SecuredMappingsEndpoint {
    RequestMappingHandlerMapping mapping;

    PreAuthorizeMappingAspect preAuthAspect;

    public SecuredMappingsEndpoint(
            @Qualifier("requestMappingHandlerMapping")
            RequestMappingHandlerMapping mapping,
            PreAuthorizeMappingAspect preAuthAspect) {
        this.mapping = mapping;
        this.preAuthAspect = preAuthAspect;
    }

    @ReadOperation
    public Map<String, Object> mappings() {
        // Lấy mapping thường
        var base = mapping.getHandlerMethods().entrySet().stream();

        // Lấy phần đã bảo mật
        var secured = preAuthAspect.getSecuredEndpoints();

        return Map.of(
                "allMappings", base.collect(Collectors.toList()),
                "preAuthorized", secured
        );
    }
}
