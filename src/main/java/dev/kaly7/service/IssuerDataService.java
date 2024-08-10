package dev.kaly7.service;

import dev.kaly7.exception.CertificateGeneratorException;
import dev.kaly7.model.IssuerData;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;

import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.function.Supplier;

public class IssuerDataService {
    private final IssuerData issuerData;


    public IssuerData getIssuerData() {
        return issuerData;
    }

    public IssuerDataService(KeysProvider keysProvider) {
        Supplier<IssuerData> generateIssuerData = () -> {
            X509Certificate cert = keysProvider.loadCertificate.get();
            PrivateKey privateKey = keysProvider.loadPrivateKey.get();

            try {
                X500Name x500Name = new JcaX509CertificateHolder(cert).getSubject();
                return new IssuerData(x500Name, privateKey);

            } catch (CertificateEncodingException ex) {
                throw new CertificateGeneratorException("Could not read issuer data from certificate", ex);
            }

        };
        this.issuerData = generateIssuerData.get();
    }


}
