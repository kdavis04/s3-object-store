package com.kdavis.objectstore.configuration;

import java.net.URI;
import java.net.URISyntaxException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@ConfigurationProperties
@Setter
public class S3Configuration {

  private String endpoint;
  private String region;
  private String accessKey;
  private String secretKey;
  @Getter private String bucket;

  @Bean
  public S3Client s3Client() throws URISyntaxException {
    return S3Client.builder()
        .endpointOverride(new URI(endpoint))
        .credentialsProvider(
            StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
        .region(Region.of(region))
        .forcePathStyle(true)
        .build();
  }
}
