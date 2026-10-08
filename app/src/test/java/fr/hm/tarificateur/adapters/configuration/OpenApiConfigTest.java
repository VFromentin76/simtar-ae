package fr.hm.tarificateur.adapters.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiConfigTest {

    private final OpenApiConfig configuration = new OpenApiConfig();

    @Test
    void shouldUseRootServerWhenContextPathIsBlank() {
        OpenAPI openAPI = configuration.customOpenAPI(" ");

        assertThat(openAPI.getServers()).singleElement()
                .extracting("url")
                .isEqualTo("/");
    }

    @Test
    void shouldUseProvidedContextPathAsServerUrl() {
        OpenAPI openAPI = configuration.customOpenAPI("/api");

        assertThat(openAPI.getServers()).singleElement()
                .extracting("url")
                .isEqualTo("/api");
    }
}
