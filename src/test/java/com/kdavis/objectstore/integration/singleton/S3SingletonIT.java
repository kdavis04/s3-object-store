package com.kdavis.objectstore.integration.singleton;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

@Slf4j
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class S3SingletonIT {

  private static final String S3_BUCKET = "test-bucket";
  private static final RestTemplate template = new RestTemplate();

  @Container
  static LocalStackContainer localStackContainer =
      new LocalStackContainer(DockerImageName.parse("localstack/localstack:1.1.0"))
          .withServices(LocalStackContainer.Service.S3);

  private static S3Client s3Client;
  @LocalServerPort int port;

  @DynamicPropertySource
  static void registerS3Properties(DynamicPropertyRegistry registry) {
    registry.add(
        "endpoint", () -> localStackContainer.getEndpointOverride(LocalStackContainer.Service.S3));
    registry.add("region", localStackContainer::getRegion);
  }

  @BeforeAll
  static void beforeAll() {
    s3Client =
        S3Client.builder()
            .endpointOverride(
                localStackContainer.getEndpointOverride(LocalStackContainer.Service.S3))
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(
                        localStackContainer.getAccessKey(), localStackContainer.getSecretKey())))
            .region(Region.of(localStackContainer.getRegion()))
            .build();

    s3Client.createBucket(b -> b.bucket(S3_BUCKET));
    //    or localStackContainer.execInContainer("awslocal", "s3", "mb", "s3://test-bucket");
  }

  @AfterEach
  void init() {
    deleteAllObjectsInBucket();
  }

  @Test
  void postObject_returnsSuccessfulResponse() throws Exception {
    final ResponseEntity<String> response = callPostObject(getFileBytes());

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertObjectCountInBucket(1);
    assertThat(getObjectFromBucket(response.getBody()), equalTo(getFileBytes()));
  }

  @Test
  void getObject_returnsSuccessfulResponse() throws Exception {
    String objectId = UUID.randomUUID().toString();
    uploadObjectToBucket(objectId);
    assertObjectCountInBucket(1);

    final ResponseEntity<byte[]> response = callGetObject(objectId);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertThat(response.getBody(), equalTo(getFileBytes()));
  }

  private ResponseEntity<byte[]> callGetObject(final String id) {
    return template.exchange(
        String.format("http://localhost:%s/objects/%s", port, id),
        HttpMethod.GET,
        new HttpEntity<>(new HttpHeaders()),
        byte[].class);
  }

  private ResponseEntity<String> callPostObject(final byte[] body) {
    return template.exchange(
        String.format("http://localhost:%s/objects", port),
        HttpMethod.POST,
        new HttpEntity<>(body, new HttpHeaders()),
        String.class);
  }

  private byte[] getObjectFromBucket(String objectId) throws Exception {
    final GetObjectRequest getObjectRequest =
        GetObjectRequest.builder().bucket(S3_BUCKET).key(objectId).build();
    final ResponseInputStream<GetObjectResponse> getObjectResponse =
        s3Client.getObject(getObjectRequest);
    return IOUtils.toByteArray(getObjectResponse);
  }

  private byte[] getFileBytes() throws Exception {
    try {
      final InputStream resource = getClass().getClassLoader().getResourceAsStream("file/test.txt");
      return IOUtils.toByteArray(resource);
    } catch (IOException e) {
      throw new Exception("An exception occurred whilst attempting to read the file.", e);
    }
  }

  private List<S3Object> listObjectsInBucket() {
    ListObjectsRequest listObjects = ListObjectsRequest.builder().bucket(S3_BUCKET).build();

    return s3Client.listObjects(listObjects).contents();
  }

  private void assertObjectCountInBucket(int count) {
    int listSize = listObjectsInBucket().size();
    assertEquals(count, listSize);
  }

  private void deleteAllObjectsInBucket() {
    if (listObjectsInBucket().size() != 0) {
      ArrayList<ObjectIdentifier> toDelete = new ArrayList<>();
      listObjectsInBucket()
          .forEach(object -> toDelete.add(ObjectIdentifier.builder().key(object.key()).build()));

      DeleteObjectsRequest request =
          DeleteObjectsRequest.builder()
              .bucket(S3_BUCKET)
              .delete(Delete.builder().objects(toDelete).build())
              .build();
      s3Client.deleteObjects(request);
    }
  }

  private void uploadObjectToBucket(String objectId) throws Exception {
    final PutObjectRequest putObjectRequest =
        PutObjectRequest.builder().bucket(S3_BUCKET).key(objectId).build();

    s3Client.putObject(putObjectRequest, RequestBody.fromBytes(getFileBytes()));
  }
}
