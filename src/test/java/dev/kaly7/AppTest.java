package dev.kaly7;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link App} class.
 * These tests verify that the main method works correctly.
 */
class AppTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUpStreams() {
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void restoreStreams() {
        System.setOut(originalOut);
    }

    @Test
    void main_shouldPrintUsageMessageWhenNoArgsProvided() {
        // Arrange
        String[] args = {};

        // Act
        App.main(args);

        // Assert
        assertTrue(outContent.toString().contains("Usage: java App <path/to/yourTppFile.json>"),
                "Should print usage message when no args provided");
    }

    @Test
    void main_shouldGenerateCertificatesWithDefaultTargetFolder() {
        // Arrange
        String tppJsonFilePath = "src/test/resources/testTpp.json";
        String[] args = {tppJsonFilePath};

        // Act
        App.main(args);

        // Assert
        // Check that the certificate files were created in the default "certs" directory
        // Note: This test assumes that the current working directory is the project root
        assertTrue(Files.exists(Paths.get("certs", "PSDAT-FAKENCA-87B2AC1", "PSDAT-FAKENCA-87B2AC1-encodedCert.pem")),
                "Certificate file should be created in default directory");
        assertTrue(Files.exists(Paths.get("certs", "PSDAT-FAKENCA-87B2AC1", "PSDAT-FAKENCA-87B2AC1-privateKey.key")),
                "Private key file should be created in default directory");
    }

    @Test
    void main_shouldGenerateCertificatesWithCustomTargetFolder() {
        // Arrange
        String tppJsonFilePath = "src/test/resources/testTpp.json";
        String targetFolder = tempDir.toString();
        String[] args = {tppJsonFilePath, "--target_folder", targetFolder};

        // Act
        App.main(args);

        // Assert
        // Check that the certificate files were created in the custom target directory
        assertTrue(Files.exists(Paths.get(targetFolder, "PSDAT-FAKENCA-87B2AC1", "PSDAT-FAKENCA-87B2AC1-encodedCert.pem")),
                "Certificate file should be created in custom directory");
        assertTrue(Files.exists(Paths.get(targetFolder, "PSDAT-FAKENCA-87B2AC1", "PSDAT-FAKENCA-87B2AC1-privateKey.key")),
                "Private key file should be created in custom directory");
    }

    @Test
    void main_shouldHandleInvalidTppJsonFilePath() {
        // Arrange
        String invalidTppJsonFilePath = "non-existent-file.json";
        String[] args = {invalidTppJsonFilePath};

        // Act & Assert
        // This should not throw an exception, but it should log an error
        assertDoesNotThrow(() -> App.main(args), "Should not throw an exception for invalid TPP JSON file path");
    }
}