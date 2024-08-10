package dev.kaly7.model;

import org.bouncycastle.asn1.x500.X500Name;

import java.security.PrivateKey;

/**
 * Represents the data associated with an issuer, including its X500 name and private key.
 * This record encapsulates the information required to identify the issuer and securely manage its key.
 *
 * <p>The {@code IssuerData} record contains two fields:</p>
 * <ul>
 *     <li>{@code x500name} - An instance of {@link X500Name} representing the issuer's distinguished name.</li>
 *     <li>{@code privateKey} - An instance of {@link PrivateKey} representing the issuer's private key.</li>
 * </ul>
 *
 * <p>Usage example:</p>
 * <pre>
 * {@code
 * X500Name issuerName = new X500Name("CN=Example Issuer, O=Example Org, C=US");
 * PrivateKey issuerKey = ...; // Obtain the private key
 * IssuerData issuerData = new IssuerData(issuerName, issuerKey);
 * }
 * </pre>
 *
 * @param x500name the X500 name of the issuer
 * @param privateKey the private key of the issuer
 */
public record IssuerData(X500Name x500name, PrivateKey privateKey) {
}
