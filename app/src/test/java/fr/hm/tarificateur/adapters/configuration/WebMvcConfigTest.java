package fr.hm.tarificateur.adapters.configuration;

import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

import static org.assertj.core.api.Assertions.assertThatCode;

class WebMvcConfigTest {

    @Test
    void shouldRegisterUploadAndOpenApiResourceHandlers() {
        WebMvcConfig config = new WebMvcConfig();
        ReflectionTestUtils.setField(config, "uploadDir", "custom-uploads");
        ResourceHandlerRegistry registry = new ResourceHandlerRegistry(new StaticApplicationContext(), null);

        assertThatCode(() -> config.addResourceHandlers(registry))
                .doesNotThrowAnyException();
    }
}
