package fr.hm.tarificateur;

import fr.hm.tarificateur.adapters.outbound.rest.ProductConfiguratorClientAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.client.RestClient;

import java.util.function.Supplier;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "external.api.soprano-configurateur.url=http://localhost/mock",
        "configurator.service.url=http://localhost:8081",
        "server.port=0"
    }
)
@ActiveProfiles("mock")
class TarificateurApplicationTest {

    @MockBean(name = "productConfiguratorRestClient")
    private RestClient productConfiguratorRestClient;

    @MockBean(name = "productConfiguratorTokenSupplier")
    private Supplier<String> productConfiguratorTokenSupplier;

    @Test
    void contextLoads() {
    }
}
