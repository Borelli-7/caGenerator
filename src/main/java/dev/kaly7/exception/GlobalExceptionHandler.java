package dev.kaly7.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.function.Function;

public class GlobalExceptionHandler {
    private static final String MESSAGE = "message";
    private static final String CODE = "code";
    private static final String DATE_TIME = "dateTime";

    private final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles a {@link CertificateGeneratorException} by logging a warning message and returning a map
     * with error details. The map is generated using the {@link #getHandlerContent} function.
     *
     * @param e the {@link CertificateGeneratorException} to handle
     * @param handlerMethod the {@link Class} of the handler method that caused the exception
     * @return a {@link Map} containing error details, including an error code, message, and timestamp
     */
    public Map<String, String> handleInvalidFormatException(InvalidFormatException e, Class<?> handlerMethod) {
        log.warn("Invalid format exception handled in service: {}, message: {}", handlerMethod.getSimpleName(), e.getMessage());
        return getHandlerContent.apply("Invalid initial data");
    }

    /**
     * Handles a {@link CertificateGeneratorException} by logging a warning message and generating
     * a map containing error information.
     *
     * <p>This method logs a warning message that includes the name of the handler method and the
     * exception message. It then uses a functional approach to generate a map with details about
     * the error, such as a predefined error code, the exception message, and the current date-time.
     *
     * @param e The {@link CertificateGeneratorException} that was thrown.
     * @param handlerMethod The {@link Class} object representing the handler method where the exception occurred.
     * @return A {@link Map} containing error details including the error code, the exception message, and the date-time.
     */
    public Map<String, String> finhandleCertificateException(CertificateGeneratorException e, Class<?> handlerMethod) {
        log.warn("Invalid format exception in certificate handled in service: {}, message: {}", handlerMethod.getSimpleName(), e.getMessage());
        return getHandlerContent.apply(e.getMessage());
    }

    private final Function<String, Map<String, String> > getHandlerContent = (message) -> Map.of(
            CODE, "400",
            MESSAGE, message,
            DATE_TIME, LocalDateTime.now().toString()
    );

}
