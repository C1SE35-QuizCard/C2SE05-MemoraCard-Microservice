package com.example.quizcards.controller.security;

import com.example.quizcards.helpers.AspectHelpers.authorize.PreAuthorizeMappingAspect;
import com.example.quizcards.helpers.AspectHelpers.authorize.SecuredMappingsSearchResult;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Endpoint(id = "secured-mappings-search-prefix")
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class SecuredMappingsSearchPrefixEndpoint {
    RequestMappingHandlerMapping mapping;

    PreAuthorizeMappingAspect aspect;

    public SecuredMappingsSearchPrefixEndpoint(
            @Qualifier("requestMappingHandlerMapping")
            RequestMappingHandlerMapping mapping,
            PreAuthorizeMappingAspect aspect) {
        this.mapping = mapping;
        this.aspect = aspect;
    }

    /**
     * GET  /actuator/secured-mappings/search-prefix?query=...
     */
    @ReadOperation
    public SecuredMappingsSearchResult searchPrefix(String query) {
        // 1) Những endpoint đã secure match prefix
        List<PreAuthorizeMappingAspect.SecuredEndpoint> securedFiltered =
                aspect.getSecuredEndpoints().stream()
                        .filter(e -> e.getPath().startsWith(query))
                        .collect(Collectors.toList());

        // 2) Tổng số endpoint (public + secured) match prefix
        long totalMatched = mapping.getHandlerMethods().entrySet().stream()
                .flatMap(entry -> {
                    RequestMappingInfo info = entry.getKey();
                    if (info.getPatternsCondition() != null) {
                        return info.getPatternsCondition().getPatterns().stream();
                    } else if (info.getPathPatternsCondition() != null) {
                        return info.getPathPatternsCondition().getPatternValues().stream();
                    }
                    return Stream.empty();
                })
                .filter(path -> path.startsWith(query))
                .distinct()
                .count();

        // 3) Build summary
        String summary = securedFiltered.size() + "/" + totalMatched;
        return new SecuredMappingsSearchResult(securedFiltered, summary);
    }
}
