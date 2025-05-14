package dev.kaly7.service;

import dev.kaly7.exception.CertificateGeneratorException;
import dev.kaly7.model.IssuerData;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the {@link IssuerDataService} class.
 * These tests verify the functionality of creating and providing IssuerData objects.
 */
@ExtendWith(MockitoExtension.class)
class IssuerDataServiceTest {

    private static KeyPair keyPair;
    private static X509Certificate certificate;

    @BeforeAll
    static void setUp() throws NoSuchAlgorithmException, OperatorCreationException, CertificateException {
        // Register BouncyCastle provider
        Security.addProvider(new BouncyCastleProvider());

        // Generate a key pair
        keyPair = generateKeyPair();

        // Generate a self-signed certificate
        certificate = generateSelfSignedCertificate(keyPair);
    }

    private static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        return keyPairGenerator.generateKeyPair();
    }

    private static X509Certificate generateSelfSignedCertificate(KeyPair keyPair) 
            throws OperatorCreationException, CertificateException {
        X500NameBuilder nameBuilder = new X500NameBuilder(BCStyle.INSTANCE);
        nameBuilder.addRDN(BCStyle.CN, "Test CN");
        nameBuilder.addRDN(BCStyle.O, "Test Organization");
        nameBuilder.addRDN(BCStyle.C, "US");
        X500Name name = nameBuilder.build();

        Date startDate = new Date();
        Date endDate = new Date(startDate.getTime() + 365 * 24 * 60 * 60 * 1000L); // 1 year validity

        X509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                name,
                BigInteger.valueOf(System.currentTimeMillis()),
                startDate,
                endDate,
                name,
                keyPair.getPublic()
        );

        ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256WithRSAEncryption")
                .build(keyPair.getPrivate());

        return new JcaX509CertificateConverter()
                .setProvider(new BouncyCastleProvider())
                .getCertificate(certBuilder.build(contentSigner));
    }

    @Test
    void constructor_shouldCreateIssuerData() {
        // Arrange
        // Create a test implementation of KeysProvider that returns our real certificate and private key
        KeysProvider keysProvider = new TestKeysProvider(certificate, keyPair.getPrivate());

        // Act
        IssuerDataService issuerDataService = new IssuerDataService(keysProvider);

        // Assert
        assertNotNull(issuerDataService, "IssuerDataService should be created");
        assertNotNull(issuerDataService.getIssuerData(), "IssuerData should not be null");
    }

    @Test
    void getIssuerData_shouldReturnIssuerData() {
        // Arrange
        // Create a test implementation of KeysProvider that returns our real certificate and private key
        KeysProvider keysProvider = new TestKeysProvider(certificate, keyPair.getPrivate());

        IssuerDataService issuerDataService = new IssuerDataService(keysProvider);

        // Act
        IssuerData issuerData = issuerDataService.getIssuerData();

        // Assert
        assertNotNull(issuerData, "IssuerData should not be null");
        assertNotNull(issuerData.x500name(), "X500Name should not be null");
        assertEquals(keyPair.getPrivate(), issuerData.privateKey(), "Private key should match the one from KeysProvider");
    }

    @Test
    void constructor_shouldThrowExceptionWhenCertificateEncodingFails() throws CertificateEncodingException {
        // Arrange
        // Create a mock certificate that throws an exception when getEncoded is called
        X509Certificate mockCertificate = mock(X509Certificate.class);
        when(mockCertificate.getEncoded()).thenThrow(new CertificateEncodingException("Test exception"));

        // Create a test implementation of KeysProvider that returns our mock certificate
        KeysProvider keysProvider = new TestKeysProvider(mockCertificate, keyPair.getPrivate());

        // Act & Assert
        assertThrows(CertificateGeneratorException.class, () -> new IssuerDataService(keysProvider),
                "Should throw CertificateGeneratorException when certificate encoding fails");
    }

    @Test
    void constructor_shouldThrowExceptionWhenLoadCertificateFails() {
        // Arrange
        // Create a test implementation of KeysProvider that throws an exception when loadCertificate is called
        KeysProvider keysProvider = new TestKeysProvider(
            () -> { throw new CertificateGeneratorException("Test exception"); },
            () -> keyPair.getPrivate()
        );

        // Act & Assert
        assertThrows(CertificateGeneratorException.class, () -> new IssuerDataService(keysProvider),
                "Should throw CertificateGeneratorException when loadCertificate fails");
    }

    @Test
    void constructor_shouldThrowExceptionWhenLoadPrivateKeyFails() {
        // Arrange
        // Create a test implementation of KeysProvider that throws an exception when loadPrivateKey is called
        KeysProvider keysProvider = new TestKeysProvider(
            () -> certificate,
            () -> { throw new CertificateGeneratorException("Test exception"); }
        );

        // Act & Assert
        assertThrows(CertificateGeneratorException.class, () -> new IssuerDataService(keysProvider),
                "Should throw CertificateGeneratorException when loadPrivateKey fails");
    }

    /**
     * A test implementation of KeysProvider that allows us to control the behavior of
     * loadCertificate and loadPrivateKey for testing purposes.
     */
    private static class TestKeysProvider extends KeysProvider {
        private final Supplier<X509Certificate> certificateSupplier;
        private final Supplier<PrivateKey> privateKeySupplier;

        public TestKeysProvider(X509Certificate certificate, PrivateKey privateKey) {
            super("test", "test"); // Use dummy paths
            this.certificateSupplier = () -> certificate;
            this.privateKeySupplier = () -> privateKey;
            this.loadCertificate = this.certificateSupplier;
            this.loadPrivateKey = this.privateKeySupplier;
        }

        public TestKeysProvider(Supplier<X509Certificate> certificateSupplier, Supplier<PrivateKey> privateKeySupplier) {
            super("test", "test"); // Use dummy paths
            this.certificateSupplier = certificateSupplier;
            this.privateKeySupplier = privateKeySupplier;
            this.loadCertificate = this.certificateSupplier;
            this.loadPrivateKey = this.privateKeySupplier;
        }
    }
}
