package dev.kaly7.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Represents a request for a certificate.
 *
 * This record encapsulates the necessary information for creating a certificate, including
 * details about authorization, roles, organization, and validity.
 *
 * @param authorizationNumber The authorization number associated with the certificate request.
 *                            This value must not be null.
 * @param roles               A list of roles associated with the certificate. The list must
 *                            contain at least one role and at most three roles. It must not be null.
 * @param organizationName    The name of the organization requesting the certificate. This value
 *                            must not be null.
 * @param organizationUnit    The organizational unit within the organization. This value can be null.
 * @param domainComponent     The domain component of the organization. This value can be null.
 * @param localityName        The locality (city or town) where the organization is located. This value can be null.
 * @param stateOrProvinceName The state or province where the organization is located. This value can be null.
 * @param countryCode         The country code where the organization is located. This value can be null.
 * @param validity            The validity period of the certificate in days. It must be between -365 and 365 days.
 *                            This value must not be null.
 * @param commonName          The common name (e.g., domain name) for which the certificate is being requested.
 *                            This value must not be null.
 * @param ocspCheckNeeded     Flag indicating whether OCSP (Online Certificate Status Protocol) checks are needed.
 *                            The default value is false.
 */
public record CertificateRequest(
        @NotNull String authorizationNumber,
        @Size(min = 1, max = 3) @NotNull List<PspRole> roles,
        @NotNull String organizationName,
        String organizationUnit,
        String domainComponent,
        String localityName,
        String stateOrProvinceName,
        String countryCode,
        @Min(-365) @Max(365) @NotNull int validity,
        @NotNull String commonName,
        boolean ocspCheckNeeded
) {
    public CertificateRequest {
    }
}