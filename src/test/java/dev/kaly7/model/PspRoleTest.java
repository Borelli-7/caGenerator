package dev.kaly7.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link PspRole} enum.
 * These tests verify that the enum contains the expected values.
 */
class PspRoleTest {

    @Test
    void enumShouldContainExpectedValues() {
        // Arrange & Act & Assert
        assertEquals(4, PspRole.values().length, "PspRole should have 4 values");
        assertNotNull(PspRole.valueOf("PISP"), "PspRole should contain PISP");
        assertNotNull(PspRole.valueOf("AISP"), "PspRole should contain AISP");
        assertNotNull(PspRole.valueOf("PIISP"), "PspRole should contain PIISP");
        assertNotNull(PspRole.valueOf("ASPSP"), "PspRole should contain ASPSP");
    }

    @Test
    void valueOf_shouldReturnCorrectEnumValue() {
        // Arrange & Act & Assert
        assertEquals(PspRole.PISP, PspRole.valueOf("PISP"), "valueOf should return PISP for 'PISP'");
        assertEquals(PspRole.AISP, PspRole.valueOf("AISP"), "valueOf should return AISP for 'AISP'");
        assertEquals(PspRole.PIISP, PspRole.valueOf("PIISP"), "valueOf should return PIISP for 'PIISP'");
        assertEquals(PspRole.ASPSP, PspRole.valueOf("ASPSP"), "valueOf should return ASPSP for 'ASPSP'");
    }

    @Test
    void valueOf_shouldThrowExceptionForInvalidValue() {
        // Arrange & Act & Assert
        assertThrows(IllegalArgumentException.class, () -> PspRole.valueOf("INVALID"),
                "valueOf should throw IllegalArgumentException for invalid value");
    }
}