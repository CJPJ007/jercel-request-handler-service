package com.jercel.tech.service;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * Client for interacting with Cloudflare R2 Storage using AWS SDK S3
 * compatibility
 */
@Service
@Slf4j
public class CloudflareR2Client {

    /**
     * Configuration class for R2 credentials and endpoint
     */
    @Value("${r2.api.access.key}")
    private String accessKey;
    @Value("${r2.api.secret.key}")
    private String secretKey;
    @Value("${r2.endpoint.url}")
    private String endpoint;
    @Value("${bucket.name}")
    private String bucketName;

    private S3Client s3Client;

    /**
     * Builds and configures the S3 client with R2-specific settings
     */
    @PostConstruct
    private void buildS3Client() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(
                accessKey,
                secretKey);

        S3Configuration serviceConfiguration = S3Configuration.builder()
                .pathStyleAccessEnabled(true)
                .build();

        s3Client = S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .region(Region.of("auto"))
                .serviceConfiguration(serviceConfiguration)
                .build();
    }

    public byte[] downloadFile(String fileName) throws IOException {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(bucketName).key(fileName)
                .build();
                
        return s3Client.getObject(getObjectRequest).readAllBytes();
    }

}