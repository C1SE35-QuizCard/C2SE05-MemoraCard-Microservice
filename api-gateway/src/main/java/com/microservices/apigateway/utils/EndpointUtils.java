package com.microservices.apigateway.utils;

import java.util.Arrays;

public class EndpointUtils {
    public static String[] convertToRegex(String[] patterns) {
        return Arrays.stream(patterns)
                .map(p -> p.endsWith("/**")
                        ? p.substring(0, p.length() - 3) + "/.*"
                        : p
                )
                .toArray(String[]::new);
    }
}
