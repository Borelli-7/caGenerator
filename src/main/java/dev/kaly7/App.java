package dev.kaly7;

import dev.kaly7.service.CertificateServiceImpl;
import dev.kaly7.service.api.CertificateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        final int ARGS_SIZE = 1;
        // Check if the required arguments are provided
        if (args.length < ARGS_SIZE) {
            logger.info("Usage: java App <path/to/yourTppFile.json> [--target_folder <target_folder>]");
            return;
        }

        String tppJsonFilePath = args[0];
        // Optional target folder argument
        String targetFolder = args.length > 1 && "--target_folder".equals(args[1]) ? args[2] : "certs";

        CertificateService certificateService = new CertificateServiceImpl();
        certificateService.generatePemFilesCerts(tppJsonFilePath, targetFolder);
    }
}