package com.kdavis.objectstore.unit.controller;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;

import com.kdavis.objectstore.controller.S3Controller;
import com.kdavis.objectstore.service.S3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class S3ControllerTest {
  private S3Controller s3Controller;
  @Mock private S3Service s3Service;

  @BeforeEach
  void setUp() {
    s3Controller = new S3Controller(s3Service);
  }

  @Test
  void putObject() {
    when(s3Service.putS3Object("test file".getBytes())).thenReturn("objectId");

    String response = s3Controller.postObject("test file".getBytes());

    assertThat(response, equalTo("objectId"));
  }

  @Test
  void getObject() {
    when(s3Service.getS3Object("objectId")).thenReturn("test file".getBytes());

    byte[] response = s3Controller.getObject("objectId");

    assertThat(response, equalTo("test file".getBytes()));
  }
}
