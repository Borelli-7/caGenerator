package dev.kaly7.service.api;

public interface CertificateService {

    void generatePemFilesCerts(String tppJsonFilePath, String targetFolder);
}
