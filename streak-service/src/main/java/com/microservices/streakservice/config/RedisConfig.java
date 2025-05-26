package com.microservices.streakservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Configuration
public class RedisConfig {
    @Bean
    public ReactiveRedisTemplate<String, Object> reactiveRedisTemplate(
            ReactiveRedisConnectionFactory reactiveRedisConnectionFactory) {

        // Tạo ObjectMapper và đăng ký JavaTimeModule
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // Sử dụng ObjectMapper đã cấu hình trong GenericJackson2JsonRedisSerializer
        GenericJackson2JsonRedisSerializer serializer =
                new GenericJackson2JsonRedisSerializer(objectMapper);

        // Build SerializationContext
        RedisSerializationContext<String, Object> serializationContext =
                RedisSerializationContext.<String, Object>newSerializationContext(new StringRedisSerializer())
                        .key(new StringRedisSerializer())
                        .value(serializer)
                        .hashKey(new StringRedisSerializer())
                        .hashValue(serializer)
                        .build();

//        return new ReactiveRedisTemplate<>(reactiveRedisConnectionFactory, serializationContext);
        ReactiveRedisTemplate<String, Object> target =
                new ReactiveRedisTemplate<>(reactiveRedisConnectionFactory, serializationContext);

        // 4) Tạo proxy quấn lấy target để catch lỗi
        ProxyFactory pf = new ProxyFactory(target);
        pf.setProxyTargetClass(true);
        pf.addAdvice((MethodInterceptor) invocation -> {
            Object ret = invocation.proceed();

            // Nếu trả về Mono → onErrorResume thành Mono.empty()
            if (ret instanceof Mono<?> mono) {
                return mono.onErrorResume(err -> Mono.empty());
            }

            // Nếu trả về Flux → onErrorResume thành Flux.empty()
            if (ret instanceof Flux<?> flux) {
                return flux.onErrorResume(err -> Flux.empty());
            }

            // Các trường hợp sync (nếu có) thì trả thẳng
            return ret;
        });

        @SuppressWarnings("unchecked")
        ReactiveRedisTemplate<String, Object> proxy =
                (ReactiveRedisTemplate<String, Object>) pf.getProxy();

        return proxy;
    }
}