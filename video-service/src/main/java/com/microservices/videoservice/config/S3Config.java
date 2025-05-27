package com.microservices.videoservice.config;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.transfer.s3.S3TransferManager;

import java.net.URI;

@Configuration
@FieldDefaults(level = AccessLevel.PRIVATE)
public class S3Config {
    @Value("${cloud.aws.credentials.access-key}")
    String accessKey;

    @Value("${cloud.aws.credentials.secret-key}")
    String secretKey;

    @Value("${cloud.aws.region.static}")
    String region;

    @Value("${cloud.aws.s3.bucket-name:default-bucket}")
    String bucketName;

    @Bean
    public S3AsyncClient s3AsyncClient() {
        AwsBasicCredentials creds = AwsBasicCredentials.create(accessKey, secretKey);
        System.out.println("Creating S3AsyncClient with region: " + region + ", bucket: " + bucketName);
        return S3AsyncClient.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(creds))
                .build();
//        boolean hasDot = bucketName.contains(".");
//        S3Configuration serviceConfig = S3Configuration.builder()
//                .pathStyleAccessEnabled(hasDot)
//                .build();
//        URI endpoint = URI.create(
//                String.format("https://%s.s3.%s.amazonaws.com", bucketName, region)
//        );
//
//        return S3AsyncClient.builder()
//                .region(Region.of(region))
//                .credentialsProvider(StaticCredentialsProvider.create(creds))
//                .serviceConfiguration(serviceConfig)
//                .endpointOverride(endpoint)
//                .build();
    }

    @Bean
    public S3TransferManager transferManager(S3AsyncClient s3AsyncClient) {
        return S3TransferManager.builder()
                .s3Client(s3AsyncClient)
                .build();
    }

    @Bean
    @Qualifier("s3BucketName")
    public String s3BucketName() {
        return bucketName;
    }

    @Bean
    @Qualifier("s3RegionStatic")
    public String s3RegionStatic() {
        return region;
    }
}
