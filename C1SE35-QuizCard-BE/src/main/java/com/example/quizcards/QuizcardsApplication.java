package com.example.quizcards;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.example.quizcards.client")
@EnableCaching
@EnableDiscoveryClient
public class QuizcardsApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuizcardsApplication.class, args);
    }

}
