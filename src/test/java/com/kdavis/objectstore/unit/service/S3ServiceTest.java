package com.kdavis.objectstore.unit.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import com.kdavis.objectstore.configuration.S3Configuration;
import com.kdavis.objectstore.service.S3Service;
import java.io.ByteArrayInputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.AbortableInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

  private final ArgumentCaptor<PutObjectRequest> putObjectRequestArgumentCaptor =
      ArgumentCaptor.forClass(PutObjectRequest.class);
  private S3Service s3Service;
  @Mock private S3Client s3Client;
  @Mock private S3Configuration s3Configuration;

  @BeforeEach
  void setUp() {
    when(s3Configuration.getBucket()).thenReturn("test-bucket");
    s3Service = new S3Service(s3Client, s3Configuration);
  }

  @Test
  void putS3Object_returnsSuccessfulResponse() {
    doReturn(PutObjectResponse.builder().build())
        .when(s3Client)
        .putObject(putObjectRequestArgumentCaptor.capture(), any(RequestBody.class));

    String response = s3Service.putS3Object("test file".getBytes());

    PutObjectRequest putObjectRequest = putObjectRequestArgumentCaptor.getValue();
    assertEquals(putObjectRequest.key(), response);
    assertEquals("test-bucket", putObjectRequest.bucket());
  }

  @Test
  void getS3Object_returnsSuccessfulResponse() {
    GetObjectRequest getObjectRequest =
        GetObjectRequest.builder().key("objectId").bucket("test-bucket").build();
    ResponseInputStream<GetObjectResponse> responseInputStream =
        new ResponseInputStream<>(
            GetObjectResponse.builder().build(),
            AbortableInputStream.create(new ByteArrayInputStream("test file".getBytes())));
    when(s3Client.getObject(getObjectRequest)).thenReturn(responseInputStream);

    byte[] response = s3Service.getS3Object("objectId");

    assertThat(response, equalTo("test file".getBytes()));
  }

  @Test
  void getS3Object_throwsException() {
    GetObjectRequest getObjectRequest =
        GetObjectRequest.builder().key("objectId").bucket("test-bucket").build();
    when(s3Client.getObject(getObjectRequest)).thenThrow(NoSuchKeyException.class);

    assertThrows(RuntimeException.class, () -> s3Service.getS3Object("objectId"));
  }
}
