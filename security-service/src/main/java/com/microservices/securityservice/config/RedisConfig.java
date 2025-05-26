package com.microservices.securityservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.benmanes.caffeine.cache.Cache;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.ReactiveRedisMessageListenerContainer;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

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
//      NO CODE: RedisSerializationContext<String, Object> serializationContext =
//                RedisSerializationContext.<String, Object>newSerializationContext(new StringRedisSerializer())
//                        .key(new StringRedisSerializer())
//                        .value(serializer)
//                        .hashKey(new StringRedisSerializer())
//                        .hashValue(serializer)
//                        .build();
//
//        return new ReactiveRedisTemplate<>(reactiveRedisConnectionFactory, serializationContext);

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

    /**
     * Khởi tạo ReactiveRedisMessageListenerContainer.
     */
    @Bean
    public ReactiveRedisMessageListenerContainer reactiveRedisContainer(
            ReactiveRedisConnectionFactory factory) {
        return new ReactiveRedisMessageListenerContainer(factory);
    }

    /**
     * Đăng ký nhận message từ hai channel: token_blacklist và token_iat.
     * Mỗi khi có PUBLISH lên một trong hai channel này, sẽ evict L1 Caffeine tương ứng.
     */
    @Bean
    public Disposable tokenCacheInvalidationSubscription(
            ReactiveRedisMessageListenerContainer container,
            CacheManager cacheManager) {

        // Định nghĩa hai pattern topic cần subscribe
        PatternTopic blacklistTopic = new PatternTopic("cache-invalidate:token_blacklist");
        PatternTopic iatTopic = new PatternTopic("cache-invalidate:token_iat");

        return container
                .receive(blacklistTopic, iatTopic)
                // Chạy evict trên scheduler riêng để không block I/O thread
                .publishOn(Schedulers.boundedElastic())
                .doOnNext(msg -> {
                    System.out.println("On message received redirected to evict cache");
                    String channel = msg.getChannel();  // ví dụ "cache-invalidate:token_blacklist"
                    String key = msg.getMessage();  // ví dụ "token_blacklist_123_abcd"
                    // cache name = phần sau dấu ":"
                    String cacheName = channel.split(":", 2)[1];    // "token_blacklist" hoặc "token_iat"

                    @SuppressWarnings("unchecked")
                    Cache<Object, Object> c = (Cache<Object, Object>) cacheManager.getCache(cacheName).getNativeCache();
                    System.out.println("Cache evict directed to: " + cacheName + " for key: " + key);
                    c.invalidate(key);
                })
                .subscribe();
    }
}