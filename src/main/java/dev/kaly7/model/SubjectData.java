package dev.kaly7.model;

import org.bouncycastle.asn1.x500.X500Name;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;

/**
 * Represents the data associated with a subject in a certificate.
 *
 * <p>This record holds the essential details required for a certificate, including
 * the private and public keys, the subject's distinguished name, serial number,
 * validity period, and whether an OCSP (Online Certificate Status Protocol) check is needed.</p>
 *
 * @param privateKey The private key associated with the subject. This key is used for signing
 *                   certificates or decrypting data encrypted with the corresponding public key.
 * @param publicKey  The public key associated with the subject. This key is used for verifying
 *                   signatures created with the corresponding private key or encrypting data
 *                   that can only be decrypted by the private key.
 * @param x500name   The X500Name representing the distinguished name of the subject. This includes
 *                   attributes such as the common name (CN), organization (O), and country (C).
 * @param serialNumber The unique serial number assigned to the certificate. This number is used
 *                     to identify the certificate and distinguish it from others issued by the same
 *                     certificate authority (CA).
 * @param startDate  The start date of the certificate's validity period. The certificate becomes
 *                   valid on this date and can be used for cryptographic operations.
 * @param endDate    The end date of the certificate's validity period. The certificate expires
 *                   after this date and can no longer be used for cryptographic operations.
 * @param ocspCheckNeeded A boolean flag indicating whether an OCSP check is required for the
 *                        certificate. If true, the certificate's status will be checked against
 *                        an OCSP responder to ensure it has not been revoked.
 */
public record SubjectData(
        PrivateKey privateKey,
        PublicKey publicKey,
        X500Name x500name,
        Integer serialNumber,
        Date startDate,
        Date endDate,
        boolean ocspCheckNeeded
) {
}