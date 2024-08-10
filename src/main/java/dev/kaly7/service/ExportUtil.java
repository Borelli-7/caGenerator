package dev.kaly7.service;

import dev.kaly7.exception.CertificateGeneratorException;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.util.function.Function;

public class ExportUtil {

    private ExportUtil() {}

    /**
     * Provides a function to export an object to a PEM-encoded string.
     * <p>
     * This method returns a {@link Function} that takes an {@link Object} as input and returns a PEM-encoded string representation of that object.
     * It uses a {@link StringWriter} and {@link JcaPEMWriter} to write the object into PEM format, and then removes newline characters from the resulting string.
     * </p>
     * <p>
     * In case of an {@link IOException} during the writing process, a {@link CertificateGeneratorException} is thrown.
     * </p>
     *
     * @return A {@link Function} that converts an {@link Object} to a PEM-encoded {@link String}.
     */
    public static Function<Object, String> exportToString() {
        return obj -> {
            try (StringWriter writer = new StringWriter();
                 JcaPEMWriter pemWriter = new JcaPEMWriter(writer)) {
                pemWriter.writeObject(obj);
                pemWriter.flush();
                // Convert the result to a string and remove newline characters
                return writer.toString().replace("\n", "");
            } catch (IOException ex) {
                throw new CertificateGeneratorException("Could not export certificate", ex);
            }
        };
    }

    /**
     * Provides a function to export an object to a byte array in PEM format.
     * <p>
     * This method creates a {@link Function} that takes an object and returns its PEM-encoded byte array representation.
     * The object is written to a {@link ByteArrayOutputStream} using {@link JcaPEMWriter}, and the resulting byte array is
     * returned. If an {@link IOException} occurs during the writing process, a {@link CertificateGeneratorException} is thrown.
     * </p>
     *
     * @return a {@link Function} that converts an {@link Object} to a byte array.
     */
    public static Function<Object, byte[]> exportToBytes() {
        return obj -> {
            try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                 JcaPEMWriter pemWriter = new JcaPEMWriter(new OutputStreamWriter(byteStream))) {
                pemWriter.writeObject(obj);
                pemWriter.flush();
                return byteStream.toByteArray();
            } catch (IOException ex) {
                throw new CertificateGeneratorException("Could not export certificate to bytes", ex);
            }
        };
    }

}
