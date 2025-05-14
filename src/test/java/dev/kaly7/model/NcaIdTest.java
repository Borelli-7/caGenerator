package dev.kaly7.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link NcaId} class.
 * These tests verify the basic functionality of the NcaId class.
 */
class NcaIdTest {

    @Test
    void constructor_shouldSetStringValue() {
        // Arrange
        String testValue = "DE-FAKENCA";

        // Act
        NcaId ncaId = new NcaId(testValue);

        // Assert
        assertEquals(testValue, ncaId.getString(), "NcaId should store the provided string value");
    }

    @Test
    void getString_shouldReturnCorrectValue() {
        // Arrange
        String testValue = "AT-FAKENCA";
        NcaId ncaId = new NcaId(testValue);

        // Act
        String result = ncaId.getString();

        // Assert
        assertEquals(testValue, result, "getString should return the value provided to the constructor");
    }

    @Test
    void equals_shouldReturnTrueForEqualObjects() {
        // Arrange
        String testValue = "FR-FAKENCA";
        NcaId ncaId1 = new NcaId(testValue);
        NcaId ncaId2 = new NcaId(testValue);

        // Act & Assert
        assertEquals(ncaId1, ncaId2, "Equal NcaId objects should be equal");
        assertEquals(ncaId1.hashCode(), ncaId2.hashCode(), "Equal objects should have the same hash code");
    }

    @Test
    void equals_shouldReturnFalseForDifferentObjects() {
        // Arrange
        NcaId ncaId1 = new NcaId("ES-FAKENCA");
        NcaId ncaId2 = new NcaId("IT-FAKENCA");

        // Act & Assert
        assertNotEquals(ncaId1, ncaId2, "Different NcaId objects should not be equal");
    }

    @Test
    void toString_shouldContainStringValue() {
        // Arrange
        String testValue = "UK-FAKENCA";
        NcaId ncaId = new NcaId(testValue);

        // Act
        String result = ncaId.toString();

        // Assert
        assertTrue(result.contains(testValue), "toString should contain the string value");
    }
}