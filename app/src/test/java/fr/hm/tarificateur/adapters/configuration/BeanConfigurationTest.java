package fr.hm.tarificateur.adapters.configuration;

import fr.hm.tarificateur.domain.service.QuotationService;
import fr.hm.tarificateur.ports.outbound.ProductConfiguratorPort;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

class BeanConfigurationTest {

    private final BeanConfiguration configuration = new BeanConfiguration();

    @Test
    void shouldCreateQuotationServiceBean() {
        ProductConfiguratorPort port = java.util.Map::of;

        QuotationService service = configuration.quotationService(port);

        assertThat(service).isNotNull();
    }

    @Test
    void shouldCreateRestClientBuilderBean() {
        RestClient.Builder builder = configuration.restClientBuilder();

        assertThat(builder).isNotNull();
    }
}
