package dev.kaly7.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link NcaName} class.
 * These tests verify the basic functionality of the NcaName class.
 */
class NcaNameTest {

    @Test
    void constructor_shouldSetStringValue() {
        // Arrange
        String testValue = "Fake NCA Authority";

        // Act
        NcaName ncaName = new NcaName(testValue);

        // Assert
        assertEquals(testValue, ncaName.getString(), "NcaName should store the provided string value");
    }

    @Test
    void getString_shouldReturnCorrectValue() {
        // Arrange
        String testValue = "Test NCA Name";
        NcaName ncaName = new NcaName(testValue);

        // Act
        String result = ncaName.getString();

        // Assert
        assertEquals(testValue, result, "getString should return the value provided to the constructor");
    }

    @Test
    void equals_shouldReturnTrueForEqualObjects() {
        // Arrange
        String testValue = "Example NCA";
        NcaName ncaName1 = new NcaName(testValue);
        NcaName ncaName2 = new NcaName(testValue);

        // Act & Assert
        assertEquals(ncaName1, ncaName2, "Equal NcaName objects should be equal");
        assertEquals(ncaName1.hashCode(), ncaName2.hashCode(), "Equal objects should have the same hash code");
    }

    @Test
    void equals_shouldReturnFalseForDifferentObjects() {
        // Arrange
        NcaName ncaName1 = new NcaName("First NCA");
        NcaName ncaName2 = new NcaName("Second NCA");

        // Act & Assert
        assertNotEquals(ncaName1, ncaName2, "Different NcaName objects should not be equal");
    }

    @Test
    void toString_shouldContainStringValue() {
        // Arrange
        String testValue = "Sample NCA Name";
        NcaName ncaName = new NcaName(testValue);

        // Act
        String result = ncaName.toString();

        // Assert
        assertTrue(result.contains(testValue), "toString should contain the string value");
    }
}