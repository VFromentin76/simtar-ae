package fr.hm.tarificateur.domain.exception;

public class ProductConfigurationNotFoundException extends DomainException {

    public ProductConfigurationNotFoundException() {
        super("Configuration produit introuvable");
    }

    public ProductConfigurationNotFoundException(String mode) {
        super("Configuration produit '" + mode + "' introuvable");
    }
}
