#!/bin/bash
awslocal s3 mb s3://test-bucket
awslocal s3 rm s3://test-bucket --recursive
awslocal s3api put-bucket-versioning --bucket test-bucket --versioning-configuration Status=Enabled