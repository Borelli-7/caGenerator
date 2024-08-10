package dev.kaly7.model;

/**
 * A record that encapsulates a certificate response.
 *
 * <p>This record holds two pieces of information:
 * <ul>
 *     <li><strong>encodedCert</strong>: The encoded certificate as a {@code String}. This is typically the certificate in a PEM or DER format.</li>
 *     <li><strong>privateKey</strong>: The private key associated with the certificate, also represented as a {@code String}. This key is usually encoded in a secure format.</li>
 * </ul>
 * </p>
 *
 * <p>The use of a record in Java provides a concise way to model immutable data with built-in methods for equality, hash code computation, and string representation.</p>
 *
 * @param encodedCert The encoded certificate.
 * @param privateKey The private key associated with the certificate.
 */
public record CertificateResponse(String encodedCert, String privateKey) {
}
