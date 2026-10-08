package fr.hm.tarificateur.ports.outbound;

import fr.hm.tarificateur.domain.model.ProductConfiguration;

import java.util.Map;

public interface ProductConfiguratorPort {
    Map<String, ProductConfiguration> getConfigurations();
}
