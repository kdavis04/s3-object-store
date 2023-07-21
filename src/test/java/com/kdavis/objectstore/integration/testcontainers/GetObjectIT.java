package com.kdavis.objectstore.integration.testcontainers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class GetObjectIT extends S3AbstractBaseIT {

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
}
