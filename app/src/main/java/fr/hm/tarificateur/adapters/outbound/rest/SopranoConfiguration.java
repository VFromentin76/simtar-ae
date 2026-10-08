package fr.hm.tarificateur.adapters.outbound.rest;

import fr.hm.commons.rest.respository.service.HmRestClientProvider;
import fr.hm.commons.token.sso.service.TokenHMKeycloakService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.client.RestClient;

import java.util.function.Supplier;

@Lazy
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "external.api.soprano-configurateur", name = "url")
@ConditionalOnBean(TokenHMKeycloakService.class)
public class SopranoConfiguration {


    private static final String SOPRANO_API = "soprano-configurateur";
    private final HmRestClientProvider hmRestClientProvider;
    private final TokenHMKeycloakService tokenHMKeycloakService;

    @Bean(name = "sopranoRestClient")
    public RestClient sopranoRestClient() {
        return hmRestClientProvider.restClientFor(SOPRANO_API);
    }

    @Bean(name = "sopranoTokenSupplier")
    public Supplier<String> sopranoTokenSupplier() {
        return () -> tokenHMKeycloakService.getToken(SOPRANO_API);
    }
}
