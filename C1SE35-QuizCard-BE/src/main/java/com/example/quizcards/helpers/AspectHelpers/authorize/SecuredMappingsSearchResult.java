package com.example.quizcards.helpers.AspectHelpers.authorize;

import java.util.List;

public record SecuredMappingsSearchResult(
        List<PreAuthorizeMappingAspect.SecuredEndpoint> endpoints,
        String summary
) {}