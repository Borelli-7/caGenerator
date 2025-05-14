package dev.kaly7.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link CertificateResponse} class.
 * These tests verify the basic functionality of the CertificateResponse record.
 */
class CertificateResponseTest {

    private static final String SAMPLE_CERT = "-----BEGIN CERTIFICATE-----\nMIIDTTCCAjWgAwIBAgIEbYmrcjANBgkqhkiG9w0BAQsFADBMMQswCQYDVQQGEwJE\n-----END CERTIFICATE-----";
    private static final String SAMPLE_KEY = "[REMOVED FOR SECURITY REASONS]";

    @Test
    void constructor_shouldSetFieldsCorrectly() {
        // Arrange & Act
        CertificateResponse response = new CertificateResponse(SAMPLE_CERT, SAMPLE_KEY);

        // Assert
        assertEquals(SAMPLE_CERT, response.encodedCert(), "Encoded certificate should match the input");
        assertEquals(SAMPLE_KEY, response.privateKey(), "Private key should match the input");
    }

    @Test
    void equals_shouldReturnTrueForEqualObjects() {
        // Arrange
        CertificateResponse response1 = new CertificateResponse(SAMPLE_CERT, SAMPLE_KEY);
        CertificateResponse response2 = new CertificateResponse(SAMPLE_CERT, SAMPLE_KEY);

        // Act & Assert
        assertEquals(response1, response2, "Equal CertificateResponse objects should be equal");
        assertEquals(response1.hashCode(), response2.hashCode(), "Equal objects should have the same hash code");
    }

    @Test
    void equals_shouldReturnFalseForDifferentObjects() {
        // Arrange
        CertificateResponse response1 = new CertificateResponse(SAMPLE_CERT, SAMPLE_KEY);
        CertificateResponse response2 = new CertificateResponse("Different cert", SAMPLE_KEY);
        CertificateResponse response3 = new CertificateResponse(SAMPLE_CERT, "Different key");

        // Act & Assert
        assertNotEquals(response1, response2, "CertificateResponse objects with different certificates should not be equal");
        assertNotEquals(response1, response3, "CertificateResponse objects with different private keys should not be equal");
    }

    @Test
    void toString_shouldContainFieldValues() {
        // Arrange
        CertificateResponse response = new CertificateResponse(SAMPLE_CERT, SAMPLE_KEY);

        // Act
        String toString = response.toString();

        // Assert
        assertTrue(toString.contains(SAMPLE_CERT), "toString should contain the encoded certificate");
        assertTrue(toString.contains(SAMPLE_KEY), "toString should contain the private key");
    }

    @Test
    void nullValues_shouldBeAccepted() {
        // Arrange & Act
        CertificateResponse response = new CertificateResponse(null, null);

        // Assert
        assertNull(response.encodedCert(), "Encoded certificate should be null");
        assertNull(response.privateKey(), "Private key should be null");
    }
}