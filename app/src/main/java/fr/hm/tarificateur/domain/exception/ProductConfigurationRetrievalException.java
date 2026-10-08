package fr.hm.tarificateur.domain.exception;

public class ProductConfigurationRetrievalException extends DomainException {

    public ProductConfigurationRetrievalException(Throwable cause) {
        super("Impossible de recuperer la configuration produit", cause);
    }

    public ProductConfigurationRetrievalException(String message, Throwable cause) {
        super(message, cause);
    }
}
