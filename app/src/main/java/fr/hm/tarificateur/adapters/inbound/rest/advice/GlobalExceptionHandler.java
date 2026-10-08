package fr.hm.tarificateur.adapters.inbound.rest.advice;

import fr.hm.tarificateur.domain.exception.ProductConfigurationNotFoundException;
import fr.hm.tarificateur.domain.exception.ProductConfigurationRetrievalException;
import fr.hm.tarificateur.domain.exception.QuotationValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String STATUS = "status";
    private static final String ERROR = "error";
    private static final String MESSAGE = "message";

    @ExceptionHandler(QuotationValidationException.class)
    public ResponseEntity<Map<String, Object>> handleQuotationValidationException(QuotationValidationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                STATUS, HttpStatus.BAD_REQUEST.value(),
                ERROR, "Bad Request",
                MESSAGE, ex.getMessage()
        ));
    }

    @ExceptionHandler(ProductConfigurationNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleProductConfigurationNotFoundException(ProductConfigurationNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
            STATUS, HttpStatus.NOT_FOUND.value(),
            ERROR, "Not Found",
            MESSAGE, ex.getMessage()
        ));
    }

    @ExceptionHandler(ProductConfigurationRetrievalException.class)
    public ResponseEntity<Map<String, Object>> handleProductConfigurationRetrievalException(ProductConfigurationRetrievalException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of(
            STATUS, HttpStatus.BAD_GATEWAY.value(),
            ERROR, "Bad Gateway",
            MESSAGE, ex.getMessage()
        ));
    }
}
