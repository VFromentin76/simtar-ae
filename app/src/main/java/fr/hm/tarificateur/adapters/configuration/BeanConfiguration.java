package fr.hm.tarificateur.adapters.configuration;

import fr.hm.tarificateur.domain.service.QuotationService;
import fr.hm.tarificateur.ports.outbound.ProductConfiguratorPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class BeanConfiguration {

    @Bean
    public QuotationService quotationService(
            @Autowired(required = false) ProductConfiguratorPort productConfiguratorPort
    ) {
        return new QuotationService(productConfiguratorPort);
    }

    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }
}
