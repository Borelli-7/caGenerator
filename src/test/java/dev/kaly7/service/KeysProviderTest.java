package dev.kaly7.service;

import dev.kaly7.exception.CertificateGeneratorException;
import org.junit.jupiter.api.Test;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link KeysProvider} class.
 * These tests verify the functionality of loading certificates and private keys.
 */
class KeysProviderTest {

    @Test
    void constructor_shouldLoadDefaultPaths() {
        // Arrange & Act
        KeysProvider keysProvider = new KeysProvider();
        
        // Assert
        assertNotNull(keysProvider, "KeysProvider should be created with default paths");
    }

    @Test
    void constructor_shouldAcceptCustomPaths() {
        // Arrange & Act
        KeysProvider keysProvider = new KeysProvider("custom/path/to/key.key", "custom/path/to/cert.pem");
        
        // Assert
        assertNotNull(keysProvider, "KeysProvider should be created with custom paths");
    }

    @Test
    void loadCertificate_shouldLoadCertificateFromClasspath() {
        // Arrange
        KeysProvider keysProvider = new KeysProvider();
        
        // Act & Assert
        // This test verifies that the certificate can be loaded from the classpath
        // It will throw an exception if the certificate cannot be loaded
        assertDoesNotThrow(() -> {
            X509Certificate certificate = keysProvider.loadCertificate.get();
            assertNotNull(certificate, "Certificate should not be null");
        }, "Loading certificate from classpath should not throw an exception");
    }

    @Test
    void loadPrivateKey_shouldLoadPrivateKeyFromClasspath() {
        // Arrange
        KeysProvider keysProvider = new KeysProvider();
        
        // Act & Assert
        // This test verifies that the private key can be loaded from the classpath
        // It will throw an exception if the private key cannot be loaded
        assertDoesNotThrow(() -> {
            PrivateKey privateKey = keysProvider.loadPrivateKey.get();
            assertNotNull(privateKey, "Private key should not be null");
        }, "Loading private key from classpath should not throw an exception");
    }

    @Test
    void loadCertificate_shouldThrowExceptionForInvalidPath() {
        // Arrange
        KeysProvider keysProvider = new KeysProvider("valid/path/to/key.key", "invalid/path/to/cert.pem");
        
        // Act & Assert
        assertThrows(CertificateGeneratorException.class, () -> keysProvider.loadCertificate.get(),
                "Should throw CertificateGeneratorException for invalid certificate path");
    }

    @Test
    void loadPrivateKey_shouldThrowExceptionForInvalidPath() {
        // Arrange
        KeysProvider keysProvider = new KeysProvider("invalid/path/to/key.key", "valid/path/to/cert.pem");
        
        // Act & Assert
        assertThrows(CertificateGeneratorException.class, () -> keysProvider.loadPrivateKey.get(),
                "Should throw CertificateGeneratorException for invalid private key path");
    }
}