package com.jercel.tech.service;

import java.io.IOException;

import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import software.amazon.awssdk.services.s3.model.S3Object;

@Service
@Slf4j
public class JercelRequestHandlerService {
     private final Tika tika;
    private final JedisPool jedisPool;
    private final Storage storage;
    private final CloudflareR2Client cloudflareR2Client;


    @Value("${redis.cache.ttl}")
    private Long REDIS_CACHE_TTL;

    @Value("${bucket.name}")
    private String BUCKET_NAME;
    public JercelRequestHandlerService(@Value("${redis.host.name}") String redisHostName, @Value("${redis.port}") int redisPort,CloudflareR2Client cloudflareR2Client){
        tika = new Tika();
        jedisPool = new JedisPool(redisHostName, redisPort);
        storage = StorageOptions.getDefaultInstance().getService();
        this.cloudflareR2Client = cloudflareR2Client;
    }

    public ResponseEntity<byte[]> serveFiles(String fileName, HttpServletRequest httpRequest) {
        try (Jedis jedis = jedisPool.getResource()) {
            log.info("Domain : {}",httpRequest.getHeader("Host"));
            String folderName = httpRequest.getHeader("Host").split("\\.")[0];

            fileName = folderName+"/build/"+ fileName;
            String redisKey = "file_cache:" +fileName;

            // Check if the file is cached in Redis
            byte[] cachedContent = jedis.get(redisKey.getBytes());
            if (cachedContent != null) {
                log.info("Cache hit: Serving " + fileName + " from Redis.");
                return createResponseEntity(fileName, cachedContent);
            }

            log.info("Cache miss: Fetching " + fileName + " from GCS.");
            // Fetch the file content from GCS
            // byte[] gcsContent = fetchFileFromGCS(fileName);
            byte[] gcsContent = fetchFile(fileName);
            // Store the file in Redis with a TTL
            jedis.setex(redisKey.getBytes(), REDIS_CACHE_TTL, gcsContent);
            System.out.println("Cached " + fileName + " in Redis.");

            return createResponseEntity(fileName, gcsContent);
        } catch (Exception e) {
            System.err.println("Error fetching file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error fetching file: " + e.getMessage()).getBytes());
        }
    }

    private byte[] fetchFileFromGCS(String fileName) {
        Blob blob = storage.get(BUCKET_NAME, fileName);
        if (blob == null) {
            throw new RuntimeException("File not found in GCS: " + fileName);
        }
        return blob.getContent();
    }

    private byte[] fetchFile(String fileName) throws IOException{
        return cloudflareR2Client.downloadFile(fileName);
    }

    private ResponseEntity<byte[]> createResponseEntity(String fileName, byte[] content) throws IOException {
        // Use Apache Tika to detect the content type
        String contentType = tika.detect(fileName);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, contentType);

        return new ResponseEntity<>(content, headers, HttpStatus.OK);
    }

}
