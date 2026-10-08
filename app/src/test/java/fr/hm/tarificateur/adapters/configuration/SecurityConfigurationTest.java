package fr.hm.tarificateur.adapters.configuration;

import org.junit.jupiter.api.Test;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SecurityConfigurationTest {

    private final SecurityConfiguration configuration = new SecurityConfiguration();

    @Test
    void shouldConfigureStatelessPermitAllSecurityFilterChain() throws Exception {
        HttpSecurity http = mock(HttpSecurity.class, RETURNS_SELF);
        SecurityFilterChain filterChain = mock(DefaultSecurityFilterChain.class);
        doReturn(filterChain).when(http).build();

        SecurityFilterChain result = configuration.securityFilterChain(http);

        assertThat(result).isSameAs(filterChain);
        verify(http).authorizeHttpRequests(any());
        verify(http).sessionManagement(any());
        verify(http).csrf(any());
        verify(http).httpBasic(any());
        verify(http).formLogin(any());
        verify(http).logout(any());
    }
}
