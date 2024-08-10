package dev.kaly7.service;

import dev.kaly7.exception.CertificateGeneratorException;
import dev.kaly7.model.*;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERUTF8String;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.slf4j.Logger;

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;


public class CertificateService {
    private static final String NCA_SHORT_NAME = "FAKENCA";
    private static final ASN1ObjectIdentifier ETSI_QC_STATEMENT = new ASN1ObjectIdentifier("0.4.0.19495.2");
    private static final SecureRandom RANDOM = new SecureRandom();

    Supplier<IssuerDataService> getIssuerDataService = () -> {
        KeysProvider keysProvider = new KeysProvider();
        return new IssuerDataService(keysProvider);
    };

    private IssuerDataService issuerDataService = getIssuerDataService.get();


    public CertificateService() {
        KeysProvider keysProvider = new KeysProvider();
        this.issuerDataService = new IssuerDataService(keysProvider);
    }

    public CertificateService(Logger logger) {
    }

    protected Supplier<KeyPair> generateKeyPair = ()->{
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048, SecureRandom.getInstance("SHA1PRNG", "SUN"));
            return keyGen.generateKeyPair();
        } catch (GeneralSecurityException ex) {
            throw new CertificateGeneratorException("Could not generate key pair", ex);
        }
    };

    private final Function<CertificateRequest, SubjectData> generateSubjectData = (cerData) ->{
        X500NameBuilder builder = new X500NameBuilder(BCStyle.INSTANCE);

        // List of pairs: each pair contains a BCStyle constant and a corresponding supplier function from cerData.
        Stream.of(
                        Map.entry(BCStyle.O, cerData.organizationName()),
                        Map.entry(BCStyle.CN, cerData.commonName()),
                        Map.entry(BCStyle.DC, cerData.domainComponent()),
                        Map.entry(BCStyle.OU, cerData.organizationUnit()),
                        Map.entry(BCStyle.C, cerData.countryCode()),
                        Map.entry(BCStyle.ST, cerData.stateOrProvinceName()),
                        Map.entry(BCStyle.L, cerData.localityName()),
                        Map.entry(BCStyle.ORGANIZATION_IDENTIFIER, cerData.authorizationNumber())
                )
                .filter(entry -> entry.getValue() != null && !entry.getValue().isBlank())
                .forEach(entry -> builder.addRDN(entry.getKey(), entry.getValue()));

        Date expiration = Date.from(
                LocalDate.now().plusDays(cerData.validity())
                        .atStartOfDay(ZoneOffset.UTC).toInstant()
        );
        KeyPair keyPairSubject = generateKeyPair.get();
        Integer serialNumber = RANDOM.nextInt(Integer.MAX_VALUE);

        return new SubjectData(
                keyPairSubject.getPrivate(), keyPairSubject.getPublic(), builder.build(),
                serialNumber, new Date(), expiration, cerData.ocspCheckNeeded()
        );
    };

    private NcaName getNcaNameFromIssuerData() {
        return new NcaName(IETFUtils.valueToString(
                issuerDataService.getIssuerData().x500name().getRDNs(BCStyle.O)[0]
                        .getFirst().getValue())
        );
    }

    private NcaId getNcaIdFromIssuerData() {
        String country = IETFUtils.valueToString(issuerDataService.getIssuerData()
                .x500name().getRDNs(BCStyle.C)[0]
                .getFirst().getValue());
        return new NcaId(country + "-" + NCA_SHORT_NAME);
    }

    private DERSequence createQcInfo(RolesOfPsp rolesOfPsp, NcaName ncaName, NcaId ncaId) {
        return new DERSequence(new ASN1Encodable[]{rolesOfPsp, ncaName, ncaId});
    }

    private static class RolesOfPsp extends DERSequence {

        static RolesOfPsp fromCertificateRequest(CertificateRequest certificateRequest) {
            List<RoleOfPsp> roles = new ArrayList<>(3); // Initialize with expected size

            List<PspRole> requestRoles = certificateRequest.roles();

            if (requestRoles.contains(PspRole.AISP)) {
                roles.add(RoleOfPsp.PSP_AI);
            }

            if (requestRoles.contains(PspRole.PISP)) {
                roles.add(RoleOfPsp.PSP_PI);
            }

            if (requestRoles.contains(PspRole.PIISP)) {
                roles.add(RoleOfPsp.PSP_IC);
            }

            if (requestRoles.contains(PspRole.ASPSP)) {
                roles.add(RoleOfPsp.PSP_AS);
            }

            return new RolesOfPsp(roles.toArray(RoleOfPsp[]::new));
        }

        RolesOfPsp(RoleOfPsp... array) {
            super(array);
        }
    }

    private static class RoleOfPsp extends DERSequence {

        static final RoleOfPsp PSP_PI = new RoleOfPsp(RoleOfPspOid.ID_PSD_2_ROLE_PSP_PI, RoleOfPspName.PSP_PI);
        static final RoleOfPsp PSP_AI = new RoleOfPsp(RoleOfPspOid.ID_PSD_2_ROLE_PSP_AI, RoleOfPspName.PSP_AI);
        static final RoleOfPsp PSP_IC = new RoleOfPsp(RoleOfPspOid.ROLE_OF_PSP_OID, RoleOfPspName.PSP_IC);
        static final RoleOfPsp PSP_AS = new RoleOfPsp(RoleOfPspOid.ID_PSD_2_ROLE_PSP_AS, RoleOfPspName.PSP_AS);

        private RoleOfPsp(RoleOfPspOid roleOfPspOid, RoleOfPspName roleOfPspName) {
            super(new ASN1Encodable[]{roleOfPspOid, roleOfPspName});
        }
    }

    private static class RoleOfPspName extends DERUTF8String {
        static final RoleOfPspName PSP_PI = new RoleOfPspName("PSP_PI");
        static final RoleOfPspName PSP_AI = new RoleOfPspName("PSP_AI");
        static final RoleOfPspName PSP_IC = new RoleOfPspName("PSP_IC");
        static final RoleOfPspName PSP_AS = new RoleOfPspName("PSP_AS");

        private RoleOfPspName(String string) {
            super(string);
        }
    }

    private static class RoleOfPspOid extends ASN1ObjectIdentifier {

        static final ASN1ObjectIdentifier ETSI_PSD_2_ROLES = new ASN1ObjectIdentifier("0.4.0.19495.1");
        static final RoleOfPspOid ID_PSD_2_ROLE_PSP_AS = new RoleOfPspOid(ETSI_PSD_2_ROLES.branch("1"));
        static final RoleOfPspOid ID_PSD_2_ROLE_PSP_PI = new RoleOfPspOid(ETSI_PSD_2_ROLES.branch("2"));
        static final RoleOfPspOid ID_PSD_2_ROLE_PSP_AI = new RoleOfPspOid(ETSI_PSD_2_ROLES.branch("3"));
        static final RoleOfPspOid ROLE_OF_PSP_OID = new RoleOfPspOid(ETSI_PSD_2_ROLES.branch("4"));

        RoleOfPspOid(ASN1ObjectIdentifier identifier) {
            super(identifier.getId());
        }
    }



}
