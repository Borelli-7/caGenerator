# Test Configuration for caGenerator

This document provides instructions for setting up and running tests for the caGenerator project.

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher

## Test Resources

The test resources directory contains the following files:

- `testTpp.json`: A sample TPP (Third Party Provider) JSON file used for testing certificate generation. This file contains various test cases with different roles, validity periods, and country codes.

## Running Tests

To run all tests, use the following Maven command from the project root:

```bash
mvn test
```

To run a specific test class, use:

```bash
mvn test -Dtest=ClassName
```

For example, to run the CertificateServiceImplTest:

```bash
mvn test -Dtest=CertificateServiceImplTest
```

## Test Structure

The tests are organized as follows:

1. **Unit Tests**:
   - Model classes: Tests for data models like CertificateRequest, CertificateResponse, PspRole, etc.
   - Service classes: Tests for utility classes like ExportUtil, KeysProvider, IssuerDataService, etc.

2. **Integration Tests**:
   - CertificateServiceImplTest: Tests the complete certificate generation flow.

## External Dependencies

The tests rely on the following external dependencies:

1. **Certificate Templates**:
   - The tests use certificate templates located in `src/main/resources/certificates/`.
   - These include `MyRootCA.key` and `MyRootCA.pem`, which are used as the issuer's private key and certificate.

2. **Bouncy Castle**:
   - The tests use the Bouncy Castle library for certificate operations.
   - This dependency is managed by Maven and does not require additional setup.

## Test Environment Setup

No special environment setup is required beyond the prerequisites mentioned above. The tests use JUnit 5's `@TempDir` annotation to create temporary directories for file output, ensuring that tests do not interfere with each other or leave artifacts in the project directory.

## Troubleshooting

If you encounter issues with the tests:

1. **Certificate Loading Issues**:
   - Ensure that the certificate templates (`MyRootCA.key` and `MyRootCA.pem`) are present in the `src/main/resources/certificates/` directory.
   - Check that these files are valid PEM-encoded certificates and private keys.

2. **File Permission Issues**:
   - Ensure that the test process has permission to create temporary directories and files.

3. **Java Version Issues**:
   - Ensure that you are using Java 17 or higher, as the project uses features from this version.