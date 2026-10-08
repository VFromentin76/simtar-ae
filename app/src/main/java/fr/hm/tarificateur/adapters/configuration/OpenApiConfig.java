package fr.hm.tarificateur.adapters.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI(@Value("${server.servlet.context-path:}") String contextPath) {
        String serverUrl = (contextPath == null || contextPath.isBlank()) ? "/" : contextPath;
        return new OpenAPI()
                .servers(List.of(new Server().url(serverUrl).description("Application Context Server")));
    }
}

