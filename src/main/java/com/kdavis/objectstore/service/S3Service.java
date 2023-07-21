package com.kdavis.objectstore.service;

import com.kdavis.objectstore.configuration.S3Configuration;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@Slf4j
public class S3Service {
  private final String bucket;
  private final S3Client s3Client;

  public S3Service(S3Client s3Client, S3Configuration s3Configuration) {
    this.s3Client = s3Client;
    this.bucket = s3Configuration.getBucket();
  }

  public String putS3Object(byte[] file) {
    String objectId = UUID.randomUUID().toString();
    PutObjectRequest putObjectRequest =
        PutObjectRequest.builder().key(objectId).bucket(bucket).build();
    try {
      s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file));
    } catch (Exception e) {
      log.error("Put S3 object threw Exception", e);
      throw new RuntimeException("Failed to store object in s3", e);
    }
    return objectId;
  }

  public byte[] getS3Object(String objectId) {
    GetObjectRequest getObjectRequest =
        GetObjectRequest.builder().key(objectId).bucket(bucket).build();
    try (ResponseInputStream<GetObjectResponse> getObjectResponse =
        s3Client.getObject(getObjectRequest)) {
      return IOUtils.toByteArray(getObjectResponse);
    } catch (NoSuchKeyException e) {
      log.error("Get S3 object threw NoSuchKeyException");
      throw new RuntimeException();
    } catch (Exception e) {
      log.error("Get S3 object threw Exception", e);
      throw new RuntimeException();
    }
  }
}
