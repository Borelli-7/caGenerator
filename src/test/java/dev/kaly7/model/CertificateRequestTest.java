package dev.kaly7.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link CertificateRequest} class.
 * These tests verify the basic functionality of the CertificateRequest record.
 */
class CertificateRequestTest {

    @Test
    void constructor_shouldSetFieldsCorrectly() {
        // Arrange & Act
        String authNumber = "PSDAT-FAKENCA-12345";
        List<PspRole> roles = List.of(PspRole.PISP);
        String orgName = "Test Organization";
        String orgUnit = "Test Unit";
        String domainComp = "test.domain.com";
        String locality = "Test City";
        String state = "Test State";
        String country = "US";
        int validity = 365;
        String commonName = "Test Common Name";
        boolean ocspCheck = false;
        
        CertificateRequest request = new CertificateRequest(
                authNumber,
                roles,
                orgName,
                orgUnit,
                domainComp,
                locality,
                state,
                country,
                validity,
                commonName,
                ocspCheck
        );

        // Assert
        assertEquals(authNumber, request.authorizationNumber(), "Authorization number should match");
        assertEquals(roles, request.roles(), "Roles should match");
        assertEquals(orgName, request.organizationName(), "Organization name should match");
        assertEquals(orgUnit, request.organizationUnit(), "Organization unit should match");
        assertEquals(domainComp, request.domainComponent(), "Domain component should match");
        assertEquals(locality, request.localityName(), "Locality should match");
        assertEquals(state, request.stateOrProvinceName(), "State should match");
        assertEquals(country, request.countryCode(), "Country code should match");
        assertEquals(validity, request.validity(), "Validity should match");
        assertEquals(commonName, request.commonName(), "Common name should match");
        assertEquals(ocspCheck, request.ocspCheckNeeded(), "OCSP check flag should match");
    }

    @Test
    void equals_shouldReturnTrueForEqualObjects() {
        // Arrange
        CertificateRequest request1 = new CertificateRequest(
                "PSDAT-FAKENCA-12345",
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
        );
        
        CertificateRequest request2 = new CertificateRequest(
                "PSDAT-FAKENCA-12345",
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
        );

        // Act & Assert
        assertEquals(request1, request2, "Equal CertificateRequest objects should be equal");
        assertEquals(request1.hashCode(), request2.hashCode(), "Equal objects should have the same hash code");
    }

    @Test
    void equals_shouldReturnFalseForDifferentObjects() {
        // Arrange
        CertificateRequest request1 = new CertificateRequest(
                "PSDAT-FAKENCA-12345",
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
        );
        
        CertificateRequest request2 = new CertificateRequest(
                "PSDAT-FAKENCA-67890", // Different authorization number
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
        );

        // Act & Assert
        assertNotEquals(request1, request2, "Different CertificateRequest objects should not be equal");
    }

    @Test
    void toString_shouldContainFieldValues() {
        // Arrange
        String authNumber = "PSDAT-FAKENCA-12345";
        List<PspRole> roles = List.of(PspRole.PISP);
        String orgName = "Test Organization";
        
        CertificateRequest request = new CertificateRequest(
                authNumber,
                roles,
                orgName,
                "Test Unit",
                "test.domain.com",
                "Test City",
                "Test State",
                "US",
                365,
                "Test Common Name",
                false
        );

        // Act
        String toString = request.toString();

        // Assert
        assertTrue(toString.contains(authNumber), "toString should contain the authorization number");
        assertTrue(toString.contains(roles.toString()), "toString should contain the roles");
        assertTrue(toString.contains(orgName), "toString should contain the organization name");
    }

    @Test
    void nullOptionalFields_shouldBeAccepted() {
        // Arrange & Act
        CertificateRequest request = new CertificateRequest(
                "PSDAT-FAKENCA-12345",
                List.of(PspRole.PISP),
                "Test Organization",
                null, // Optional field
                null, // Optional field
                null, // Optional field
                null, // Optional field
                null, // Optional field
                365,
                "Test Common Name",
                false
        );

        // Assert
        assertNull(request.organizationUnit(), "Organization unit should be null");
        assertNull(request.domainComponent(), "Domain component should be null");
        assertNull(request.localityName(), "Locality should be null");
        assertNull(request.stateOrProvinceName(), "State should be null");
        assertNull(request.countryCode(), "Country code should be null");
    }
}