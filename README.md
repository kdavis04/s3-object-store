# S3 Object Store

A demo SpringBoot S3 object storage application using the AWS SDK for Java v2 and LocalStack. 
The purpose of this project is to demonstrate how LocalStack can be used as a cloud emulator to test AWS applications, and to demonstrate different ways of using the Testcontainers module for LocalStack in automated testing.

## Prerequisites

- Java 17
- AWS CLI (optional)

## Running the application

First you will need to clone this repository to your local machine by running the following command in your terminal:

`git clone https://github.com/kdavis04/s3-object-store.git`

Once cloned, ensure you have connection to a Docker daemon and run the following command in the project root directory terminal (where the docker-compose.yml file is located):

`docker-compose up`

This will spin up a LocalStack container on port 4566, which is where the AWS S3 client points to. The script `setup-localstack.sh` is executed on container start-up to create an S3 bucket named 'test-bucket', which is the bucket the S3 client uploads/gets files to/from in this demo project.

Once the LocalStack container is running, you can run the application, either by running the main ObjectstoreApplication class from your IDE, by the bootRun Gradle task, or by running the following command in the project root directory terminal: `./gradlew bootRun`. The application will start on port 8080.

## Exposed endpoints

The following endpoints are exposed by the application on port 8080:

- POST /objects - Uploads the given file to the S3 bucket
- GET /objects/{objectId} - Gets the object with the given objectId from the S3 bucket - the objectId is returned from uploading an object

## Tests

Tests are written using JUnit 5 and Mockito. Integration tests use the Testcontainers library module for LocalStack so require connection to a Docker daemon.

Run all tests using the test Gradle task, or run the following command in the project root directory terminal: `./gradlew test`.

### Unit tests

The repo contains unit tests for the S3 controller and service classes for completeness.  

### Integration tests

Integration tests use the Testcontainers module for LocalStack to spin up a LocalStack container within our JUnit tests. The integration tests test the S3 controller and service classes together, and test the application against the emulated AWS S3 service (LocalStack container).

This project demonstrates 2 ways to run integration tests with Testcontainers:
- Using the JUnit 5 @Testcontainers annotation on a test class to start a container once before any test method and stop after the last test method has executed
- Using a singleton container to define a container that is started once for several test classes and stopped after all test classes have executed

## LocalStack commands

The following commands can be run in the LocalStack container terminal to interact with the emulated AWS S3 service:

- Create a bucket: `awslocal s3 mb s3://test-bucket`
- List available buckets: `awslocal s3 ls`
- View content of a bucket: `awslocal s3 ls s3://test-bucket`
- Download a file from a bucket: `awslocal s3 cp s3://test-bucket/{objectKey} {localFilePath}`

### AWS CLI commands

If you have AWS CLI installed, or wish to practise with it then you can also run the above commands in any terminal, replacing `awslocal` with `aws --endpoint-url=http://localhost:4566`. For example, to create a bucket you can run:
`aws --endpoint-url=http://localhost:4566 s3 mb s3://test-bucket`.

You will need to ensure you have the AWS CLI configured for the LocalStack container, by running `aws configure` and providing any dummy value for the AWS Access and Secret Access Keys, and any valid AWS region like eu-west-1. LocalStack doesn't validate these credentials but a profile is required so all values must be non-blank regardless. 
