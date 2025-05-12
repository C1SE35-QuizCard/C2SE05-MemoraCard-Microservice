package com.example.quizcards.helpers.AspectHelpers.authorize;

import jakarta.annotation.PostConstruct;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.condition.RequestMethodsRequestCondition;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.*;

@Aspect
@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PreAuthorizeMappingAspect {
    RequestMappingHandlerMapping handlerMapping;

    public PreAuthorizeMappingAspect(
            @Qualifier("requestMappingHandlerMapping")
            RequestMappingHandlerMapping handlerMapping) {
        this.handlerMapping = handlerMapping;
    }

    /**
     * Danh sách các endpoint có @PreAuthorize
     */
    @Getter
    private final List<SecuredEndpoint> securedEndpoints = new ArrayList<>();

    @PostConstruct
    public void init() {
        handlerMapping.getHandlerMethods().forEach((info, handlerMethod) -> {
            // tìm @PreAuthorize trên method hoặc class
            PreAuthorize preAuth = AnnotationUtils.findAnnotation(
                    handlerMethod.getMethod(), PreAuthorize.class
            );
            if (preAuth == null) {
                preAuth = AnnotationUtils.findAnnotation(
                        handlerMethod.getBeanType(), PreAuthorize.class
                );
            }
            if (preAuth != null) {
                Set<String> paths;
                if (info.getPatternsCondition() != null) {
                    paths = info.getPatternsCondition().getPatterns();
                } else if (info.getPathPatternsCondition() != null) {
                    paths = info.getPathPatternsCondition().getPatternValues();
                } else {
                    return;
                }

                // lấy methods, phòng trường hợp null
                Set<RequestMethod> methods = Optional.of(info.getMethodsCondition())
                        .map(RequestMethodsRequestCondition::getMethods)
                        .orElse(Collections.emptySet());

                String expr = preAuth.value();
                paths.forEach(path ->
                        securedEndpoints.add(new SecuredEndpoint(path, methods, expr))
                );
            }
        });
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SecuredEndpoint {
        private String path;
        private Set<RequestMethod> methods;
        private String preAuthorizeExpression;
    }
}
