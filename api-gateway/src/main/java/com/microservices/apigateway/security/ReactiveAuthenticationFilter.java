// FILE này được sử dụng để lọc cấu hình global, hiện không còn sử dụng tới nữa, nhưng vẫn dùng để tham khảo kiến thức.

//package com.microservices.apigateway.security;
//
//import com.microservices.apigateway.client.SecurityClient;
//import com.microservices.apigateway.configuration.UrlFilter;
//import com.microservices.apigateway.exception.ErrorHandler;
//import com.microservices.apigateway.utils.EndpointUtils;
//import com.microservices.security.model.UserInfo;
//import com.microservices.security.model.UserPrincipal;
//import lombok.AccessLevel;
//import lombok.RequiredArgsConstructor;
//import lombok.experimental.FieldDefaults;
//import lombok.experimental.NonFinal;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.cloud.gateway.filter.GatewayFilterChain;
//import org.springframework.cloud.gateway.filter.GlobalFilter;
//import org.springframework.core.Ordered;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.server.reactive.ServerHttpRequest;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.context.ReactiveSecurityContextHolder;
//import org.springframework.stereotype.Component;
//import org.springframework.util.CollectionUtils;
//import org.springframework.util.StringUtils;
//import org.springframework.web.reactive.function.client.WebClientResponseException;
//import org.springframework.web.server.ServerWebExchange;
//import org.springframework.web.server.WebFilter;
//import org.springframework.web.server.WebFilterChain;
//import reactor.core.publisher.Mono;
//
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@Component
//@Slf4j
//@RequiredArgsConstructor
//@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
//public class ReactiveAuthenticationFilter implements GlobalFilter, Ordered {
//    SecurityClient securityClient;
//
//    private static final String[] publicEndpointsRegex =
//            EndpointUtils.convertToRegex(UrlFilter.publicEndpoints);
//
//    private boolean isPublicEndpoint(ServerHttpRequest request) {
//        return Arrays.stream(publicEndpointsRegex)
//                .anyMatch(s -> request.getURI().getPath().matches(s));
//    }
//
//    @Override
//    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
//        log.info("Enter authentication filter....");
//
//        if (isPublicEndpoint(exchange.getRequest())) {
//            return chain.filter(exchange);
//        }
//
//        String jwt = getAuthorization(exchange);
//        if (jwt == null) {
//            return chain.filter(exchange);
//        }
//
//        return securityClient.validateToken(jwt)
//                .flatMap(response -> {
//                    UserInfo userInfo = response.getBody();
//                    HttpStatus status = (HttpStatus) response.getStatusCode();
//
//                    if (userInfo == null) {
//                        return ErrorHandler.handleAuthenticationError(exchange.getResponse(), "Invalid JWT token", HttpStatus.UNAUTHORIZED);
//                    }
//
//                    if (status.isError()) {
//                        Map<String, Object> error = determineError(status);
//                        String errorMessage = (String) error.get("error");
//
//                        return ErrorHandler.handleAuthenticationError(exchange.getResponse(), errorMessage, status);
//                    }
//
//                    if (!userInfo.getIsEnabled()) {
//                        return ErrorHandler.handleAuthenticationError(exchange.getResponse(), "User is disabled", HttpStatus.UNAUTHORIZED);
//                    }
//
//                    ServerHttpRequest mutatedRequest = mutateRequestWithHeaders(exchange.getRequest(), userInfo);
//                    ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();
//
////                  NO CODE: return chain.filter(mutatedExchange)
////                            .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authToken));
//                    return chain.filter(mutatedExchange);
//                })
//                .onErrorResume(WebClientResponseException.class, ex -> {
//                    String message = "Authentication service error: " + ex.getStatusCode().value() + " " + ex.getStatusText();
//
//                    return ErrorHandler.handleAuthenticationError(exchange.getResponse(), message, (HttpStatus) ex.getStatusCode());
//                })
//                .onErrorResume(Exception.class, ex -> ErrorHandler.handleAuthenticationError(exchange.getResponse(), "An unexpected error occurred during authentication.", HttpStatus.INTERNAL_SERVER_ERROR));
//    }
//
//    private Map<String, Object> determineError(HttpStatus status) {
//        Map<String, Object> error = new HashMap<>();
//        String message = "";
//        error.put("status", status.value());
////        NO CODE: error.put("error", status.getReasonPhrase());
//        if (status.is5xxServerError()) {
//            message = "Authentication service encountered an error.";
//        } else if (status.equals(HttpStatus.FORBIDDEN)) {
//            message = "User is not authorized.";
//        } else if (status.equals(HttpStatus.UNAUTHORIZED)) {
//            message = "Invalid token.";
//        } else if (status.equals(HttpStatus.NOT_FOUND)) {
//            message = "User not found.";
//        } else if (status.equals(HttpStatus.BAD_REQUEST)) {
//            message = "Bad request.";
//        } else if (status.equals(HttpStatus.INTERNAL_SERVER_ERROR)) {
//            message = "Internal server error.";
//        }
//        error.put("error", message);
//        return error;
//    }
//
//    private ServerHttpRequest mutateRequestWithHeaders(ServerHttpRequest request, UserInfo userInfo) {
//        ServerHttpRequest.Builder builder = request.mutate();
//        builder.header("X-User-Id", String.valueOf(userInfo.getId()));
//        builder.header("X-Username", userInfo.getUserName());
//        if (userInfo.getRoles() != null) {
//            builder.header("X-Authorities-Roles", String.join(",", userInfo.getRoles()));
//        } else {
//            builder.header("X-Authorities-Roles", "");
//        }
//        if (userInfo.getPermissions() != null) {
//            builder.header("X-Authorities-Permissions", String.join(",", userInfo.getPermissions()));
//        } else {
//            builder.header("X-Authorities-Permissions", "");
//        }
//        builder.header("X-User-Enabled", String.valueOf(userInfo.getIsEnabled()));
//        builder.header("X-User-FirstName", userInfo.getFirstName());
//        builder.header("X-User-LastName", userInfo.getLastName());
//        builder.header("X-User-Avatar", userInfo.getAvatar());
//        builder.header("X-User-Code", userInfo.getUserCode());
//        builder.header("X-User-Gender", Boolean.toString(userInfo.getGender()));
//
//        builder.header("X-User-Email", userInfo.getEmail());
//        builder.header("X-User-PhoneNumber", userInfo.getPhoneNumber());
//        builder.header("X-User-Address", userInfo.getAddress());
//
//        return builder.build();
//    }
//
////   NO CODE: private String getJwtFromRequest(ServerWebExchange exchange) {
////        List<String> authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION);
////        if (CollectionUtils.isEmpty(authHeader)) {
////            return null;
////        }
////
////        String bearerToken = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
////        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
////            return bearerToken.substring(7);
////        }
////        return null;
////    }
//
//    private String getAuthorization(ServerWebExchange exchange) {
//        List<String> authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION);
//        if (CollectionUtils.isEmpty(authHeader)) {
//            return null;
//        }
//
//        String bearerToken = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
//        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
//            return bearerToken;
//        }
//        return null;
//    }
//
//    @Override
//    public int getOrder() {
//        return Ordered.HIGHEST_PRECEDENCE + 100;
//    }
//}
