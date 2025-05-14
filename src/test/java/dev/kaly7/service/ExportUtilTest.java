package dev.kaly7.service;

import dev.kaly7.exception.CertificateGeneratorException;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link ExportUtil} class.
 * These tests verify the functionality of the utility methods for exporting certificates and keys.
 */
class ExportUtilTest {

    @Test
    void exportToString_shouldConvertObjectToPemString() throws NoSuchAlgorithmException {
        // Arrange
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        
        // Act
        Function<Object, String> exportFunction = ExportUtil.exportToString();
        String result = exportFunction.apply(keyPair.getPrivate());
        
        // Assert
        assertNotNull(result, "Export result should not be null");
        assertTrue(result.contains("PRIVATE KEY"), "Exported string should contain 'PRIVATE KEY'");
    }

    @Test
    void exportToBytes_shouldConvertObjectToPemBytes() throws NoSuchAlgorithmException {
        // Arrange
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        
        // Act
        Function<Object, byte[]> exportFunction = ExportUtil.exportToBytes();
        byte[] result = exportFunction.apply(keyPair.getPrivate());
        
        // Assert
        assertNotNull(result, "Export result should not be null");
        assertTrue(result.length > 0, "Exported bytes should not be empty");
        
        // Convert bytes to string to verify content
        String resultString = new String(result);
        assertTrue(resultString.contains("PRIVATE KEY"), "Exported bytes should contain 'PRIVATE KEY' when converted to string");
    }

    @Test
    void exportToString_shouldThrowExceptionForNonExportableObject() {
        // Arrange
        Object nonExportableObject = new Object() {
            @Override
            public String toString() {
                throw new RuntimeException("Test exception");
            }
        };
        
        // Act & Assert
        Function<Object, String> exportFunction = ExportUtil.exportToString();
        assertThrows(CertificateGeneratorException.class, () -> exportFunction.apply(nonExportableObject),
                "Should throw CertificateGeneratorException for non-exportable object");
    }

    @Test
    void exportToBytes_shouldThrowExceptionForNonExportableObject() {
        // Arrange
        Object nonExportableObject = new Object() {
            @Override
            public String toString() {
                throw new RuntimeException("Test exception");
            }
        };
        
        // Act & Assert
        Function<Object, byte[]> exportFunction = ExportUtil.exportToBytes();
        assertThrows(CertificateGeneratorException.class, () -> exportFunction.apply(nonExportableObject),
                "Should throw CertificateGeneratorException for non-exportable object");
    }
}