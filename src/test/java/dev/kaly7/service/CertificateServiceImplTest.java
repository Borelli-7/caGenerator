package dev.kaly7.service;

import dev.kaly7.model.CertificateRequest;
import dev.kaly7.model.CertificateResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class CertificateServiceImplTest {

    @Mock
    private BiFunction<String, String, Optional<String>> validateInputsMock;

    @Mock
    private Function<List<InputStream>, List<CertificateRequest>> parseJsonFileMock;

    @Mock
    private Function<String, List<InputStream>> getInputStreamsMock;

    @Mock
    private Function<List<CertificateRequest>, List<CertificateResponse>> generateCertificateMock;

    @Mock
    private Function<List<CertificateRequest>, List<String>> getAuthorizationNumbersMock;

    @Mock
    Consumer<String> fileWriterMock;

    @InjectMocks
    private CertificateServiceImpl certificateService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Disabled
    @Test
    void testGeneratePemFilesCerts_ValidInputs() {
        List<String> expectedAuthorizationNumber = List.of ("PSDAT-FAKENCA-87B2AC", "PSDDE-FAKENCA-87B2QX" );
        String tppJsonFilePath = Objects.requireNonNull(getClass().getClassLoader()
                .getResource("testTpp.json")).getPath();
        String targetFolder = "target/certs-folder";

        // Mocking dependencies

        when(validateInputsMock.apply(tppJsonFilePath, targetFolder)).thenReturn(Optional.of(tppJsonFilePath));
        when(getInputStreamsMock.apply(tppJsonFilePath)).thenReturn(Collections.singletonList(mock(InputStream.class)));
        when(parseJsonFileMock.apply(any())).thenReturn(Collections.singletonList(mock(CertificateRequest.class)));
        CertificateResponse responseMock = mock(CertificateResponse.class);
        when(generateCertificateMock.apply(any())).thenReturn(Collections.singletonList(mock(responseMock)));
        String certificate = mock(responseMock.encodedCert());
        String privateKey = mock(responseMock.privateKey());
        when(getAuthorizationNumbersMock.apply(any())).thenReturn(expectedAuthorizationNumber);


        // Execute the method under test
        certificateService.generatePemFilesCerts(tppJsonFilePath, targetFolder);

        //verify(validateInputsMock).apply(tppJsonFilePath, targetFolder);
        verify(getInputStreamsMock).apply(tppJsonFilePath);
        verify(parseJsonFileMock).apply(any());
        verify(generateCertificateMock).apply(any());
    }

    @Test
    void testGeneratePemFilesCerts_InvalidInputs() {
        String tppJsonFilePath = "";
        String targetFolder = "valid/path/to/target";

        // Mocking dependencies
        when(validateInputsMock.apply(tppJsonFilePath, targetFolder)).thenReturn(Optional.empty());

        // Execute the method under test
        certificateService.generatePemFilesCerts(tppJsonFilePath, targetFolder);

        // Verify that further processing is not done
        verify(getInputStreamsMock, never()).apply(anyString());
        verify(parseJsonFileMock, never()).apply(any());
        verify(generateCertificateMock, never()).apply(any());
    }

    @Disabled
    @Test
    void testSaveCertificateAsPem_ValidInputs() throws IOException {
        String targetFolder = "valid/path/to/target";
        String certificate = "mock certificate content";
        String pemFileName = "certificate.pem";
        String tppUnit = "unit";

        // Mock the method to ensure it doesn't do actual file operations
        doNothing().when(certificateService).saveCertificateAsPem(anyString(), anyString(), anyString(), anyString());

        // Execute the method under test
        certificateService.saveCertificateAsPem(targetFolder, certificate, pemFileName, tppUnit);

        // Verify that the method was called with the correct parameters
        verify(certificateService).saveCertificateAsPem(targetFolder, certificate, pemFileName, tppUnit);
    }

    @Test
    void testHandleFile_EmptyInputStreams() {
        Supplier<List<InputStream>> inputStreamSuppliersMock = mock(Supplier.class);
        when(inputStreamSuppliersMock.get()).thenReturn(Collections.emptyList());

        Optional<?> result = certificateService.handleFile(
                inputStreamSuppliersMock,
                parseJsonFileMock,
                generateCertificateMock,
                mock(BiConsumer.class)
        );

        assertTrue(result.isEmpty(), "Expected empty result when input streams are empty");
    }

}
