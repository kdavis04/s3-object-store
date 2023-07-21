package com.kdavis.objectstore.integration.testcontainers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class PostObjectIT extends S3AbstractBaseIT {

  @Test
  void postObject_returnsSuccessfulResponse() throws Exception {
    final ResponseEntity<String> response = callPostObject(getFileBytes());

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertObjectCountInBucket(1);
    assertThat(getObjectFromBucket(response.getBody()), equalTo(getFileBytes()));
  }

  private ResponseEntity<String> callPostObject(final byte[] body) {
    return template.exchange(
        String.format("http://localhost:%s/objects", port),
        HttpMethod.POST,
        new HttpEntity<>(body, new HttpHeaders()),
        String.class);
  }
}
