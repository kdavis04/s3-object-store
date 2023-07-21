package com.kdavis.objectstore.controller;

import com.kdavis.objectstore.service.S3Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/objects")
public class S3Controller {

  private final S3Service s3Service;

  public S3Controller(S3Service s3Service) {
    this.s3Service = s3Service;
  }

  @PostMapping
  public String postObject(@RequestBody byte[] file) {
    return s3Service.putS3Object(file);
  }

  @GetMapping("/{objectId}")
  public byte[] getObject(@PathVariable("objectId") String objectId) {
    return s3Service.getS3Object(objectId);
  }
}
