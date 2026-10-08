package fr.hm.tarificateur.adapters.outbound.rest;

import fr.hm.tarificateur.ports.outbound.ProductConfiguratorPort;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestClient;

import java.util.function.Supplier;

@Configuration
public class ProductConfiguratorClientConfiguration {

    @Bean(name = "productConfiguratorRestClient")
    @Profile("mock")
    public RestClient mockProductConfiguratorRestClient(
        RestClient.Builder restClientBuilder,
        @Value("${configurator.service.url:http://localhost:8081}") String configuratorBaseUrl
    ) {
        return restClientBuilder.baseUrl(configuratorBaseUrl).build();
    }

    @Bean(name = "productConfiguratorTokenSupplier")
    @Profile("mock")
    public Supplier<String> mockProductConfiguratorTokenSupplier() {
        return () -> null;
    }

    @Bean
    @Profile("mock")
    public ProductConfiguratorPort mockProductConfiguratorPort(
        @Qualifier("productConfiguratorRestClient") RestClient restClient,
        @Qualifier("productConfiguratorTokenSupplier") Supplier<String> tokenSupplier
    ) {
        return new ProductConfiguratorClientAdapter(restClient, tokenSupplier);
    }

    @Bean(name = "productConfiguratorRestClient")
    @Profile("!mock")
    public RestClient authenticatedProductConfiguratorRestClient(
        @Qualifier("sopranoRestClient") RestClient sopranoRestClient
    ) {
        return sopranoRestClient;
    }

    @Bean(name = "productConfiguratorTokenSupplier")
    @Profile("!mock")
    public Supplier<String> authenticatedProductConfiguratorTokenSupplier(
        @Qualifier("sopranoTokenSupplier") Supplier<String> sopranoTokenSupplier
    ) {
        return sopranoTokenSupplier;
    }

    @Bean
    @Profile("!mock")
    public ProductConfiguratorPort authenticatedProductConfiguratorPort(
        @Qualifier("productConfiguratorRestClient") RestClient restClient,
        @Qualifier("productConfiguratorTokenSupplier") Supplier<String> tokenSupplier
    ) {
        return new ProductConfiguratorClientAdapter(restClient, tokenSupplier);
    }
}
