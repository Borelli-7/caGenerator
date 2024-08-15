package dev.kaly7.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.kaly7.exception.CertificateGeneratorException;
import dev.kaly7.model.*;
import dev.kaly7.service.api.CertificateService;
import org.bouncycastle.asn1.*;
import org.bouncycastle.asn1.ocsp.OCSPObjectIdentifiers;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.qualified.QCStatement;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;
import java.util.function.*;
import java.util.stream.Stream;


public class CertificateServiceImpl implements CertificateService {
    private static final String NCA_SHORT_NAME = "FAKENCA";
    private static final ASN1ObjectIdentifier ETSI_QC_STATEMENT = new ASN1ObjectIdentifier("0.4.0.19495.2");
    private static final SecureRandom RANDOM = new SecureRandom();

    Supplier<IssuerDataService> getIssuerDataService = () -> {
        KeysProvider keysProvider = new KeysProvider();
        return new IssuerDataService(keysProvider);
    };

    private IssuerDataService issuerDataService;

    {
        getIssuerDataService.get();
    }

    private final Logger logger = LoggerFactory.getLogger(CertificateServiceImpl.class);

    public CertificateServiceImpl() {
        KeysProvider keysProvider = new KeysProvider();
        this.issuerDataService = new IssuerDataService(keysProvider);
    }

    Function<List<InputStream>, List<CertificateRequest>>  parseJsonFile = (jsonFileStreams)-> {

        List<CertificateRequest> certificateRequests = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        jsonFileStreams.forEach( jsonFileStream ->
                {
                    try {
                        certificateRequests.add(objectMapper.readValue(jsonFileStream, CertificateRequest.class));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
        return certificateRequests;
    };


    Function<String, List<InputStream>> getInputStreams = (tppJsonFilePath) -> {
        List<InputStream> inputStreams = new ArrayList<>();
        Path path = Paths.get(tppJsonFilePath);
        try {
            // Read the JSON file
            String jsonContent = new String(Files.readAllBytes(path));

            // Parse JSON
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonArray = objectMapper.readTree(jsonContent);

            // Create InputStreams for each JSON object
            for (JsonNode jsonNode : jsonArray) {
                // Convert JSON node to string and then to InputStream
                String jsonString = jsonNode.toString();
                InputStream inputStream = new ByteArrayInputStream(jsonString.getBytes());
                inputStreams.add(inputStream);
            }

        } catch (IOException e) {
            throw new RuntimeException("Json File not found or unable to read: " + tppJsonFilePath, e);
        }
        return inputStreams;
    };


    private void saveCertificateAsPem(String targetFolder, String certificate, String pemFileName, String tppUnit) throws IOException {
        StringBuilder tppFolder = new StringBuilder(targetFolder);
        Path targetPath;
        if (targetFolder.endsWith("/")) {
            targetPath = Paths.get(tppFolder.append(tppUnit).toString());
        }else {
            targetPath = Paths.get(tppFolder.append("/").append(tppUnit).toString());
        }
        createDirectories(targetPath);

        Path filepath = targetPath.resolve(pemFileName);

        writeToFile(filepath, certificate,
                content -> {
                    try {
                        writeFile(content, filepath);
                    } catch (IOException e) {
                        logger.error("Error writing the certificate to PEM file: {}", filepath, e);
                        throw new RuntimeException(e);
                    }
                }
        );
    }

    private void createDirectories(Path path) throws IOException {
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            logger.error("Error creating directories: {}", path, e);
            throw e;
        }
    }

    private void writeToFile(Path filepath, String content, Consumer<String> fileWriter) {
        fileWriter.accept(content);
        logger.info("PEM file created: {}", filepath);
    }

    private void writeFile(String content, Path filepath) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(filepath)) {
            writer.write(content);
        }
    }

    private void savePemFiles(String targetFolder, List<CertificateResponse> responses, List<String> authNumbers) {

        int i = 0;
        while (i < responses.size()) {

            CertificateResponse finalResponse = responses.get(i);
            String authNumber = authNumbers.get(i);
            saveToFile(
                    authNumber+"-encodedCert.pem",
                    certFileName -> logAndSave(targetFolder, certFileName, finalResponse::encodedCert, authNumber)
            );

            saveToFile(
                    authNumber+"-privateKey.key",
                    keyFileName -> logAndSave(targetFolder, keyFileName, finalResponse::privateKey, authNumber)
            );
            i++;
        }
    }

    private void saveToFile(String fileName, Consumer<String> fileSaver) {
        fileSaver.accept(fileName);
    }

    private void logAndSave(String targetFolder, String fileName, Supplier<String> contentSupplier, String tppUnit) {
        try {
            logger.info("Saving file to: {}", fileName);
            saveCertificateAsPem(targetFolder, contentSupplier.get(), fileName, tppUnit);
        } catch (IOException e) {
            logger.error("Error saving file: {}", fileName, e);
        }
    }

    private final Function<List<CertificateRequest>, List<String>> getAuthorizationNumbers = (requests) ->
            requests.stream()
                    .map(CertificateRequest::authorizationNumber)
                    .toList();

    /**
     * Generates PEM files for certificates based on the provided JSON file and saves them to the specified target folder.
     *
     * <p>This method performs the following steps:
     * <ol>
     *   <li>Validates the input parameters: `tppJsonFilePath` and `targetFolder`. If either of these is invalid, an error is logged, and the method returns.</li>
     *   <li>Attempts to process the JSON file located at `tppJsonFilePath`. This involves opening the file, parsing it into a `CertificateRequest`, generating a `CertificateResponse`, and saving the result as PEM files.</li>
     *   <li>Logs the status of the operation, including whether the certificate generation was successful or if an error occurred during the process.</li>
     * </ol>
     *
     * @param tppJsonFilePath the path to the JSON file containing the certificate data. This cannot be null or empty. If invalid, an error will be logged.
     * @param targetFolder the directory path where the generated PEM files will be saved. This cannot be null or empty. If invalid, an error will be logged.
     */
    @Override
    public void generatePemFilesCerts(String tppJsonFilePath, String targetFolder) {
        validateInputs.apply(tppJsonFilePath, targetFolder)
                .flatMap(path -> handleFile(
                        () -> getInputStreams.apply(path),
                        jsonFileStreams -> parseJsonFile.apply(jsonFileStreams),
                        requests -> generateCertificate.apply(requests),
                        (responses, requests) -> savePemFiles(targetFolder, responses, getAuthorizationNumbers.apply(requests))
                ))
                .ifPresentOrElse(
                        success -> logger.info("Certificate generation completed successfully."),
                        ()-> logger.error("Error during certificate generation")
                );
    }

    private final BiFunction<String, String, Optional<String>> validateInputs = (tppJsonFilePath, targetFolder)-> {
        if (tppJsonFilePath == null || tppJsonFilePath.isEmpty()) {
            logger.error("TPP JSON file path is null or empty.");
            return Optional.empty();
        }
        if (targetFolder == null || targetFolder.isEmpty()) {
            logger.error("Target folder is null or empty.");
            return Optional.empty();
        }
        return Optional.of(tppJsonFilePath);
    };

    private <T> Optional<T> handleFile(
            Supplier<List<InputStream>> inputStreamSuppliers,
            Function<List<InputStream>, List<CertificateRequest>> jsonParsers,
            Function<List<CertificateRequest>, List<CertificateResponse>> certGenerator,
            BiConsumer<List<CertificateResponse>, List<CertificateRequest>> pemSaver) {
        List<InputStream> jsonFileStreams = inputStreamSuppliers.get();
        if (jsonFileStreams.isEmpty()) {
            logger.error("TPP JSON file not found.");
            return Optional.empty();
        }

        List<CertificateRequest> requests = jsonParsers.apply(jsonFileStreams);
        List<CertificateResponse> responses = certGenerator.apply(requests);

        logger.info("Certificate generated successfully");
        pemSaver.accept(responses, requests);

        return Optional.of((T) responses);
    }

    private final Supplier<KeyPair> generateKeyPair = ()->{
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
            Map<PspRole, RoleOfPsp> roleMapping = Map.of(
                    PspRole.AISP, RoleOfPsp.PSP_AI,
                    PspRole.PISP, RoleOfPsp.PSP_PI,
                    PspRole.PIISP, RoleOfPsp.PSP_IC,
                    PspRole.ASPSP, RoleOfPsp.PSP_AS
            );

            RoleOfPsp[] rolesArray = certificateRequest.roles().stream()
                    .filter(roleMapping::containsKey)
                    .map(roleMapping::get)
                    .toArray(RoleOfPsp[]::new);

            return new RolesOfPsp(rolesArray);
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

    private final Function<CertificateRequest, QCStatement> generateQcStatement = (certificateRequest) ->{
        NcaName ncaName = getNcaNameFromIssuerData();
        NcaId ncaId = getNcaIdFromIssuerData();
        ASN1Encodable qcStatementInfo = createQcInfo(
                RolesOfPsp.fromCertificateRequest(certificateRequest), ncaName, ncaId
        );

        return new QCStatement(ETSI_QC_STATEMENT, qcStatementInfo);
    };

    /**
     * A lambda function for generating an X.509 certificate based on provided subject data and QC statement.
     *
     * <p>This function takes a {@link SubjectData} object and a {@link QCStatement} as input, and performs
     * the following steps to generate an X.509 certificate:</p>
     * <ol>
     *     <li>Retrieves issuer data from the {@code issuerDataService}.</li>
     *     <li>Creates a {@link ContentSigner} using the issuer's private key.</li>
     *     <li>Constructs an {@link X509v3CertificateBuilder} with the provided subject data and issuer data.</li>
     *     <li>Adds the QC statement extension to the certificate builder.</li>
     *     <li>Conditionally adds the OCSP no-check extension if the subject data indicates that OCSP check is not needed.</li>
     *     <li>Builds the certificate and converts it to an {@link X509Certificate} using a certificate converter.</li>
     * </ol>
     *
     * <p>If an exception occurs during the certificate generation process, it is wrapped and re-thrown as a
     * {@link CertificateGeneratorException} with a descriptive message.</p>
     *
     * @see IssuerData
     * @see ContentSigner
     * @see X509v3CertificateBuilder
     * @see Extension
     * @see DERSequence
     * @see CertificateGeneratorException
     */
    private final BiFunction<SubjectData, QCStatement, X509Certificate> generateX509Certificate = (subjectData, statement) -> {
        try {
            IssuerData issuerData = issuerDataService.getIssuerData();

            // Create the ContentSigner and CertificateBuilder
            ContentSigner contentSigner = createContentSigner(issuerData);
            X509v3CertificateBuilder certGen = createCertificateBuilder(subjectData, issuerData);

            // Add the QC statement extension
            certGen.addExtension(Extension.qCStatements, false, new DERSequence(statement));

            // Conditionally add the OCSP extension if needed
            if (!subjectData.ocspCheckNeeded()) {
                certGen.addExtension(createOcspNoCheckExtension());
            }

            // Build the certificate and convert it to X509Certificate
            return convertToX509Certificate(certGen, contentSigner);

        } catch (Exception ex) {
            throw new CertificateGeneratorException("Could not create certificate", ex);
        }
    };

    /**
     * A lambda function for generating a certificate response based on a given certificate request.
     *
     * <p>This function takes a {@link CertificateRequest} object as input, processes it to generate
     * the necessary subject data and QC statement, creates an X.509 certificate using these details,
     * and then constructs a {@link CertificateResponse} containing the exported certificate and private key
     * as strings.</p>
     *
     * <p>The lambda expression performs the following steps:</p>
     * <ol>
     *     <li>Calls the {@code generateSubjectData} function to obtain {@link SubjectData} from the
     *     {@link CertificateRequest}.</li>
     *     <li>Calls the {@code generateQcStatement} function to obtain {@link QCStatement} from the
     *     {@link CertificateRequest}.</li>
     *     <li>Uses the {@code generateX509Certificate} function to generate an X.509 certificate using
     *     the obtained {@link SubjectData} and {@link QCStatement}.</li>
     *     <li>Exports the generated certificate and the subject's private key to strings using
     *     {@link ExportUtil#exportToString()}.</li>
     *     <li>Creates and returns a new {@link CertificateResponse} object containing the exported
     *     certificate and private key strings.</li>
     * </ol>
     *
     * @see CertificateRequest
     * @see CertificateResponse
     * @see SubjectData
     * @see QCStatement
     * @see X509Certificate
     * @see ExportUtil
     *
     */
    public Function<List<CertificateRequest>, List<CertificateResponse>> generateCertificate = certificateRequests->
        certificateRequests.stream()
                .map(certificateRequest -> {
                    SubjectData subjectData = generateSubjectData.apply(certificateRequest);
                    QCStatement qcStatement = generateQcStatement.apply(certificateRequest);
                    X509Certificate cert = generateX509Certificate.apply(subjectData, qcStatement);
                    return new CertificateResponse(
                            ExportUtil.exportToString().apply(cert),
                            ExportUtil.exportToString().apply(subjectData.privateKey())
                    );
                })
                .toList();


    private ContentSigner createContentSigner(IssuerData issuerData) throws OperatorCreationException {
        return new JcaContentSignerBuilder("SHA256WithRSAEncryption").build(issuerData.privateKey());
    }

    private X509v3CertificateBuilder createCertificateBuilder(SubjectData subjectData, IssuerData issuerData) {
        return new JcaX509v3CertificateBuilder(
                issuerData.x500name(),
                new BigInteger(subjectData.serialNumber().toString()),
                subjectData.startDate(),
                subjectData.endDate(),
                subjectData.x500name(),
                subjectData.publicKey()
        );
    }

    private Extension createOcspNoCheckExtension() throws IOException {
        return new Extension(
                OCSPObjectIdentifiers.id_pkix_ocsp_nocheck,
                false,
                new DEROctetString(DERNull.INSTANCE)
        );
    }

    private X509Certificate convertToX509Certificate(X509v3CertificateBuilder certGen, ContentSigner contentSigner) throws CertificateException {
        X509CertificateHolder certHolder = certGen.build(contentSigner);
        JcaX509CertificateConverter certConverter = new JcaX509CertificateConverter();
        return certConverter.getCertificate(certHolder);
    }


}
