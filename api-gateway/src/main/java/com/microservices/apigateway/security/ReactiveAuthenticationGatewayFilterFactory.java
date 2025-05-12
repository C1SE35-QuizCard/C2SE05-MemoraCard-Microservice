package com.microservices.apigateway.security;


import com.microservices.apigateway.client.SecurityClient;
import com.microservices.apigateway.configuration.UrlFilter;
import com.microservices.apigateway.exception.ErrorHandler;
import com.microservices.apigateway.utils.EndpointUtils;
import com.microservices.security.model.UserInfo;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.text.MessageFormat;
import java.util.*;

@Slf4j
@Component("ReactiveAuthentication")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReactiveAuthenticationGatewayFilterFactory
        extends AbstractGatewayFilterFactory<ReactiveAuthenticationGatewayFilterFactory.Config> {
    SecurityClient securityClient;

    @Value("${app.api-prefix-v1}")
    @NonFinal
    String apiPrefixV1;

    public static class Config {
        // nếu cần thêm property, khai báo ở đây
    }

    public ReactiveAuthenticationGatewayFilterFactory(SecurityClient securityClient) {
        super(Config.class);    // quan trọng: cho Spring biết Config là class này
        this.securityClient = securityClient;
    }

    //    NO CODE: private static final String[] publicEndpointsRegex =
    //                  EndpointUtils.convertToRegex(UrlFilter.publicEndpoints);

    //    NO CODE: private boolean isPublicEndpoint(ServerHttpRequest request) {
    //        return Arrays.stream(publicEndpointsRegex)
    //                .anyMatch(s -> request.getURI().getPath().matches(s));
    //    }

    private static final String[] authEndpointsRegex =
            EndpointUtils.convertToRegex(UrlFilter.authEndpoints);

    private boolean isAuthEndpoint(ServerHttpRequest request) {
        return Arrays.stream(authEndpointsRegex)
                .anyMatch(s -> request.getURI().getPath().matches(apiPrefixV1 + s));
    }

    @Override
    public Config newConfig() {
        return new Config();
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            log.info("Enter authentication filter 2 ....");

            ServerHttpRequest request = exchange.getRequest();

            // Bỏ qua public endpoints
            if (!isAuthEndpoint(exchange.getRequest())) {
                return chain.filter(exchange);
            }

            // Lấy JWT
            String bearer = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (!StringUtils.hasText(bearer) || !bearer.startsWith("Bearer ")) {
                return chain.filter(exchange);
            }

            return securityClient.validateToken(bearer)
                    .flatMap(resp -> {
                        UserInfo userInfo = resp.getBody();
                        HttpStatus status = (HttpStatus) resp.getStatusCode();

                        if (userInfo == null) {
                            return ErrorHandler.handleAuthenticationError(
                                    exchange.getResponse(),
                                    "Invalid JWT token",
                                    HttpStatus.UNAUTHORIZED
                            );
                        }
                        if (status.isError()) {
                            Map<String, Object> err = determineError(status);
                            return ErrorHandler.handleAuthenticationError(
                                    exchange.getResponse(),
                                    (String) err.get("error"),
                                    status
                            );
                        }
                        if (!userInfo.getIsEnabled()) {
                            return ErrorHandler.handleAuthenticationError(
                                    exchange.getResponse(),
                                    "User is disabled",
                                    HttpStatus.UNAUTHORIZED
                            );
                        }

                        // Gắn headers và auth
                        ServerHttpRequest mutated = mutateRequestWithHeaders(request, userInfo);

                        return chain.filter(exchange.mutate().request(mutated).build());
                    })
                    .onErrorResume(WebClientResponseException.class, ex -> {
                        String msg = MessageFormat.format(
                                "Authentication service error: {0} {1}",
                                ex.getStatusCode().value(), ex.getStatusText()
                        );
                        return ErrorHandler.handleAuthenticationError(
                                exchange.getResponse(),
                                msg,
                                (HttpStatus) ex.getStatusCode()
                        );
                    })
                    .onErrorResume(Exception.class, ex ->
                            ErrorHandler.handleAuthenticationError(
                                    exchange.getResponse(),
                                    "Unexpected error during authentication.",
                                    HttpStatus.INTERNAL_SERVER_ERROR
                            )
                    );
        };
    }

    private ServerHttpRequest mutateRequestWithHeaders(
            ServerHttpRequest request, UserInfo userInfo) {
        ServerHttpRequest.Builder b = request.mutate();
        b.header("X-User-Id", String.valueOf(userInfo.getId()));
        b.header("X-Username", userInfo.getUserName());
        b.header("X-Authorities-Roles",
                String.join(",", Optional.ofNullable(userInfo.getRoles()).orElse(List.of())));
        b.header("X-Authorities-Permissions",
                String.join(",", Optional.ofNullable(userInfo.getPermissions()).orElse(List.of())));
        b.header("X-User-Enabled", String.valueOf(userInfo.getIsEnabled()));
        b.header("X-User-FirstName", userInfo.getFirstName());
        b.header("X-User-LastName", userInfo.getLastName());
        b.header("X-User-Avatar", userInfo.getAvatar());
        b.header("X-User-Code", userInfo.getUserCode());
        b.header("X-User-Gender", Boolean.toString(userInfo.getGender()));
        b.header("X-User-Email", userInfo.getEmail());
        b.header("X-User-PhoneNumber", userInfo.getPhoneNumber());
        b.header("X-User-Address", userInfo.getAddress());
        return b.build();
    }

    private Map<String, Object> determineError(HttpStatus status) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", status.value());
        String msg;
        if (status.is5xxServerError()) {
            msg = "Authentication service encountered an error.";
        } else if (status == HttpStatus.FORBIDDEN) {
            msg = "User is not authorized.";
        } else if (status == HttpStatus.UNAUTHORIZED) {
            msg = "Invalid token.";
        } else if (status == HttpStatus.NOT_FOUND) {
            msg = "User not found.";
        } else if (status == HttpStatus.BAD_REQUEST) {
            msg = "Bad request.";
        } else {
            msg = status.getReasonPhrase();
        }
        map.put("error", msg);
        return map;
    }

}
