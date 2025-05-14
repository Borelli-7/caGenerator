package dev.kaly7.service;

import dev.kaly7.model.CertificateRequest;
import dev.kaly7.model.CertificateResponse;
import dev.kaly7.model.PspRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the {@link CertificateServiceImpl} class.
 * These tests verify the complete certificate generation flow.
 */
class CertificateServiceImplTest {

    private CertificateServiceImpl certificateService;
    
    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        certificateService = new CertificateServiceImpl();
    }

    @Test
    void generatePemFilesCerts_shouldGenerateCertificatesFromJsonFile() {
        // Arrange
        String tppJsonFilePath = "src/test/resources/testTpp.json";
        String targetFolder = tempDir.toString();
        
        // Act
        certificateService.generatePemFilesCerts(tppJsonFilePath, targetFolder);
        
        // Assert
        // Check that the certificate files were created
        assertTrue(Files.exists(Paths.get(targetFolder, "PSDAT-FAKENCA-87B2AC1", "PSDAT-FAKENCA-87B2AC1-encodedCert.pem")),
                "Certificate file should be created");
        assertTrue(Files.exists(Paths.get(targetFolder, "PSDAT-FAKENCA-87B2AC1", "PSDAT-FAKENCA-87B2AC1-privateKey.key")),
                "Private key file should be created");
        
        assertTrue(Files.exists(Paths.get(targetFolder, "PSDAT-FAKENCA-87B2AC2", "PSDAT-FAKENCA-87B2AC2-encodedCert.pem")),
                "Certificate file should be created");
        assertTrue(Files.exists(Paths.get(targetFolder, "PSDAT-FAKENCA-87B2AC2", "PSDAT-FAKENCA-87B2AC2-privateKey.key")),
                "Private key file should be created");
        
        assertTrue(Files.exists(Paths.get(targetFolder, "PSDAT-FAKENCA-87B2AC3", "PSDAT-FAKENCA-87B2AC3-encodedCert.pem")),
                "Certificate file should be created");
        assertTrue(Files.exists(Paths.get(targetFolder, "PSDAT-FAKENCA-87B2AC3", "PSDAT-FAKENCA-87B2AC3-privateKey.key")),
                "Private key file should be created");
        
        assertTrue(Files.exists(Paths.get(targetFolder, "PSDAT-FAKENCA-87B2AC4", "PSDAT-FAKENCA-87B2AC4-encodedCert.pem")),
                "Certificate file should be created");
        assertTrue(Files.exists(Paths.get(targetFolder, "PSDAT-FAKENCA-87B2AC4", "PSDAT-FAKENCA-87B2AC4-privateKey.key")),
                "Private key file should be created");
    }

    @Test
    void generateCertificate_shouldGenerateCertificateResponses() {
        // Arrange
        List<CertificateRequest> requests = Arrays.asList(
                new CertificateRequest(
                        "PSDAT-TEST-12345",
                        List.of(PspRole.PISP),
                        "Test Organization",
                        "Test Unit",
                        "test.domain.com",
                        "Test City",
                        "Test State",
                        "US",
                        365,
                        "Test Common Name",
                        false
                ),
                new CertificateRequest(
                        "PSDAT-TEST-67890",
                        Arrays.asList(PspRole.PISP, PspRole.AISP),
                        "Test Organization 2",
                        "Test Unit 2",
                        "test2.domain.com",
                        "Test City 2",
                        "Test State 2",
                        "DE",
                        365,
                        "Test Common Name 2",
                        true
                )
        );
        
        // Act
        List<CertificateResponse> responses = certificateService.generateCertificate.apply(requests);
        
        // Assert
        assertNotNull(responses, "Certificate responses should not be null");
        assertEquals(2, responses.size(), "Should generate 2 certificate responses");
        
        // Check the first response
        assertNotNull(responses.get(0).encodedCert(), "Encoded certificate should not be null");
        assertNotNull(responses.get(0).privateKey(), "Private key should not be null");
        assertTrue(responses.get(0).encodedCert().contains("CERTIFICATE"), "Encoded certificate should contain 'CERTIFICATE'");
        assertTrue(responses.get(0).privateKey().contains("PRIVATE KEY"), "Private key should contain 'PRIVATE KEY'");
        
        // Check the second response
        assertNotNull(responses.get(1).encodedCert(), "Encoded certificate should not be null");
        assertNotNull(responses.get(1).privateKey(), "Private key should not be null");
        assertTrue(responses.get(1).encodedCert().contains("CERTIFICATE"), "Encoded certificate should contain 'CERTIFICATE'");
        assertTrue(responses.get(1).privateKey().contains("PRIVATE KEY"), "Private key should contain 'PRIVATE KEY'");
    }

    @Test
    void generatePemFilesCerts_shouldHandleInvalidInputs() {
        // Arrange
        String nullTppJsonFilePath = null;
        String emptyTppJsonFilePath = "";
        String nonExistentTppJsonFilePath = "non-existent-file.json";
        String nullTargetFolder = null;
        String emptyTargetFolder = "";
        String validTppJsonFilePath = "src/test/resources/testTpp.json";
        String validTargetFolder = tempDir.toString();
        
        // Act & Assert
        // Test with null TPP JSON file path
        certificateService.generatePemFilesCerts(nullTppJsonFilePath, validTargetFolder);
        // No exception should be thrown, but no files should be created
        
        // Test with empty TPP JSON file path
        certificateService.generatePemFilesCerts(emptyTppJsonFilePath, validTargetFolder);
        // No exception should be thrown, but no files should be created
        
        // Test with non-existent TPP JSON file path
        certificateService.generatePemFilesCerts(nonExistentTppJsonFilePath, validTargetFolder);
        // No exception should be thrown, but no files should be created
        
        // Test with null target folder
        certificateService.generatePemFilesCerts(validTppJsonFilePath, nullTargetFolder);
        // No exception should be thrown, but no files should be created
        
        // Test with empty target folder
        certificateService.generatePemFilesCerts(validTppJsonFilePath, emptyTargetFolder);
        // No exception should be thrown, but no files should be created
    }
}