package dev.kaly7.service;

import com.nimbusds.jose.util.X509CertUtils;
import dev.kaly7.exception.CertificateGeneratorException;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Properties;
import java.util.function.Supplier;

public class KeysProvider {
    private static final Logger logger = LoggerFactory.getLogger(KeysProvider.class);

    private String issuerPrivateKey;
    private String issuerCertificate;

    public KeysProvider(String issuerPrivateKey, String issuerCertificate){
        this.issuerPrivateKey = issuerPrivateKey;
        this.issuerCertificate = issuerCertificate;
    }

    public KeysProvider() {
        Properties properties = new Properties();
        try (InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream("application.yml")) {
            if (input == null) {
                throw new FileNotFoundException("application.yml not found in classpath");
            }
            properties.load(input);
            issuerPrivateKey = properties.getProperty("qwac.certificate-generator.template.public.key", "certificates/MyRootCA.key");
            issuerCertificate = properties.getProperty("qwac.certificate-generator.template.private.key", "certificates/MyRootCA.pem");

            logger.info("public key and private key properties load successfully");
        } catch (IOException e) {
            logger.error("Error loading public and private key property : {}", e.getMessage(), e);
        }
    }

    /**
     * Supplier for loading a private key from a PEM file.
     *
     * <p>This Supplier reads a PEM-encoded private key from a resource file and converts it
     * into a Java {@link PrivateKey} object. The PEM file is located using the current thread's
     * context class loader and its path is specified by {@code issuerPrivateKey}.
     * The BouncyCastle provider is used for parsing the PEM file and converting it into a key pair.
     *
     * <p>If an error occurs while reading the file or parsing the key, an
     * {@link CertificateGeneratorException} is thrown.
     */
    public Supplier<PrivateKey> loadPrivateKey = () -> {
        try (InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(issuerPrivateKey);
             BufferedReader br = new BufferedReader(new InputStreamReader(stream));
             PEMParser pp = new PEMParser(br)) {

            // Add BouncyCastle provider for PEM parsing
            Security.addProvider(new BouncyCastleProvider());
            // Read PEM-encoded key pair from the PEM file
            PEMKeyPair pemKeyPair = (PEMKeyPair) pp.readObject();
            // Convert the PEM key pair to a Java KeyPair
            KeyPair kp = new JcaPEMKeyConverter().getKeyPair(pemKeyPair);
            return kp.getPrivate();
        } catch (IOException ex) {
            throw new CertificateGeneratorException("Could not load private key", ex);
        }
    };

    /**
     * Supplier to load an X509Certificate from the classpath.
     * <p>
     * This supplier reads a certificate file specified by the {@code issuerCertificate} resource name,
     * converts it to a byte array, and then parses it into an {@code X509Certificate} object.
     * <p>
     * The process involves the following steps:
     * <ul>
     *     <li>Obtains an input stream for the certificate file from the classpath.</li>
     *     <li>Reads the entire input stream into a byte array.</li>
     *     <li>Parses the byte array into an {@code X509Certificate} object using the {@code X509CertUtils.parse} method.</li>
     * </ul>
     * If an {@code IOException} occurs during reading or parsing, a {@code CertificateGeneratorException} is thrown.
     * <p>
     * This supplier is useful for deferring the loading of the certificate until it is needed,
     * encapsulating the certificate loading logic in a functional interface.
     */
    public Supplier<X509Certificate> loadCertificate = () -> {
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(issuerCertificate)) {
            byte[] bytes = IOUtils.toByteArray(is);
            return X509CertUtils.parse(bytes);
        } catch (IOException ex) {
            throw new CertificateGeneratorException("Could not read certificate from classpath", ex);
        }
    };

}
