package fr.hm.tarificateur.adapters.inbound.rest.advice;

import fr.hm.tarificateur.domain.exception.ProductConfigurationNotFoundException;
import fr.hm.tarificateur.domain.exception.ProductConfigurationRetrievalException;
import fr.hm.tarificateur.domain.exception.QuotationValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("Should handle QuotationValidationException and return 400 Bad Request")
    void testHandleQuotationValidationExceptionReturnsBadRequest() {
        // Arrange
        String errorMessage = "Invalid quotation: missing required fields";
        QuotationValidationException exception = new QuotationValidationException(errorMessage);

        // Act
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleQuotationValidationException(exception);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody())
                .containsEntry("status", 400)
                .containsEntry("error", "Bad Request")
                .containsEntry("message", errorMessage);
    }

    @Test
    @DisplayName("Should return correct status code 400")
    void testHandleQuotationValidationExceptionStatusCode() {
        // Arrange
        QuotationValidationException exception = new QuotationValidationException("Test error");

        // Act
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleQuotationValidationException(exception);

        // Assert
        assertThat(response.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    @DisplayName("Should include exception message in response body")
    void testHandleQuotationValidationExceptionIncludesMessage() {
        // Arrange
        String customMessage = "Custom validation error message";
        QuotationValidationException exception = new QuotationValidationException(customMessage);

        // Act
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleQuotationValidationException(exception);

        // Assert
        assertThat(response.getBody().get("message")).isEqualTo(customMessage);
    }

    @Test
    @DisplayName("Should handle QuotationValidationException with empty message")
    void testHandleQuotationValidationExceptionWithNullMessage() {
        // Arrange
        String emptyMessage = "";
        QuotationValidationException exception = new QuotationValidationException(emptyMessage);

        // Act
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleQuotationValidationException(exception);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsKey("message");
        assertThat(response.getBody().get("message")).isEqualTo("");
    }

    @Test
    @DisplayName("Should have all required response fields")
    void testHandleQuotationValidationExceptionHasAllRequiredFields() {
        // Arrange
        QuotationValidationException exception = new QuotationValidationException("Error");

        // Act
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleQuotationValidationException(exception);

        // Assert
        Map<String, Object> body = response.getBody();
        assertThat(body)
                .containsKeys("status", "error", "message")
                .hasSize(3);
    }

    @Test
    void shouldHandleProductConfigurationNotFoundException() {
        ProductConfigurationNotFoundException exception = new ProductConfigurationNotFoundException("CRD");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleProductConfigurationNotFoundException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody())
            .containsEntry("status", 404)
            .containsEntry("error", "Not Found")
            .containsEntry("message", "Configuration produit 'CRD' introuvable");
    }

    @Test
    void shouldHandleProductConfigurationRetrievalException() {
        ProductConfigurationRetrievalException exception =
            new ProductConfigurationRetrievalException(new RuntimeException("boom"));

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleProductConfigurationRetrievalException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(response.getBody())
            .containsEntry("status", 502)
            .containsEntry("error", "Bad Gateway")
            .containsEntry("message", "Impossible de recuperer la configuration produit");
    }
}
