package fr.hm.tarificateur.adapters.outbound.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.hm.tarificateur.domain.exception.ProductConfigurationNotFoundException;
import fr.hm.tarificateur.domain.exception.ProductConfigurationRetrievalException;
import fr.hm.tarificateur.domain.model.ProductConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductConfiguratorClientAdapter Tests")
class ProductConfiguratorClientAdapterTest {

    private static final Class<ProductConfiguratorClientAdapter.ProductConfigurationResponseDto> RESPONSE_TYPE =
        ProductConfiguratorClientAdapter.ProductConfigurationResponseDto.class;

    @Mock
    private RestClient restClient;
    @Mock
    private Supplier<String> tokenSupplier;
    @Mock
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;
    @Mock
    private RestClient.RequestHeadersSpec requestHeadersSpec;
    @Mock
    private RestClient.ResponseSpec responseSpec;

    private ProductConfiguratorClientAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new ProductConfiguratorClientAdapter(restClient, tokenSupplier);
    }

    @Test
    void shouldMapBothCiAndCrdConfigurationsFromWrapper() {
        var ci = product("PROD-CI", "CODE-CI", "CI", "2026-01-01", List.of(), List.of());
        var crd = product("PROD-CRD", "CODE-CRD", "CRD", "2026-01-01", List.of(), List.of());
        mockRestClientChain(new ProductConfiguratorClientAdapter.ProductConfigurationResponseDto(ci, crd));

        Map<String, ProductConfiguration> result = adapter.getConfigurations();

        assertThat(result).containsOnlyKeys("PROD-CI", "PROD-CRD");
        assertThat(result.get("PROD-CI").modeCalcul()).isEqualTo("CI");
        assertThat(result.get("PROD-CRD").modeCalcul()).isEqualTo("CRD");
        verify(restClient, times(1)).get();
        verify(requestHeadersUriSpec).uri("/v2/assurance-pret/versions-produit/offres/PROTEMP/actives");
        verify(requestHeadersSpec).header(HttpHeaders.AUTHORIZATION, "Bearer token");
        verify(requestHeadersSpec).accept(MediaType.APPLICATION_JSON);
    }

    @Test
    void shouldCallMockConfiguratorWithoutAuthorizationHeaderWhenTokenIsAbsent() {
        var dto = product("PROD-001", "CODE-001", "CI", "2026-01-01", List.of(), List.of());
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/v2/assurance-pret/versions-produit/offres/PROTEMP/actives"))
            .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(RESPONSE_TYPE))
            .thenReturn(new ProductConfiguratorClientAdapter.ProductConfigurationResponseDto(dto, null));
        when(tokenSupplier.get()).thenReturn(null);

        assertThat(adapter.getConfigurations()).containsKey("PROD-001");
        verify(requestHeadersUriSpec).uri("/v2/assurance-pret/versions-produit/offres/PROTEMP/actives");
        verify(requestHeadersSpec, never()).header(eq(HttpHeaders.AUTHORIZATION), anyString());
    }

    @Test
    void shouldUseCodeAsFallbackKeyWhenCodeProduitIsNull() {
        var dto = product(null, "CODE-001", null, null, List.of(), List.of());
        mockRestClientChain(new ProductConfiguratorClientAdapter.ProductConfigurationResponseDto(dto, null));

        Map<String, ProductConfiguration> result = adapter.getConfigurations();

        assertThat(result).containsKey("CODE-001");
        assertThat(result.get("CODE-001").modeCalcul()).isEqualTo("CI");
    }

    @Test
    void shouldPreserveGenericRacLists() {
        List<Map<String, Object>> eligibilites = List.of(Map.of("code", "RAC-ELIG", "eligible", true));
        List<Map<String, Object>> coefficients = List.of(Map.of("code", "RAC-COEFF", "coefficient", 125));
        var dto = product("PROD-001", "CODE-001", "CI", "2026-01-01", eligibilites, coefficients);
        mockRestClientChain(new ProductConfiguratorClientAdapter.ProductConfigurationResponseDto(dto, null));

        ProductConfiguration result = adapter.getConfigurations().get("PROD-001");

        assertThat(result.racEligibilites()).containsExactlyElementsOf(eligibilites);
        assertThat(result.racCoefficients()).containsExactlyElementsOf(coefficients);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenConfiguratorReturns404() {
        mockRequest();
        when(responseSpec.body(RESPONSE_TYPE)).thenThrow(HttpClientErrorException.create(
            org.springframework.http.HttpStatus.NOT_FOUND, "Not Found", HttpHeaders.EMPTY, null, null));

        assertThatThrownBy(adapter::getConfigurations)
            .isInstanceOf(ProductConfigurationNotFoundException.class)
            .hasMessage("Configuration produit introuvable");
    }

    @Test
    void shouldDescribeMissingCalculationModeWithoutAnIdentifier() {
        assertThat(new ProductConfigurationNotFoundException("CI"))
            .hasMessage("Configuration produit 'CI' introuvable");
        assertThat(new ProductConfigurationNotFoundException("CRD"))
            .hasMessage("Configuration produit 'CRD' introuvable");
    }

    @Test
    void shouldThrowRetrievalExceptionWhenConfiguratorCallFails() {
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/v2/assurance-pret/versions-produit/offres/PROTEMP/actives"))
            .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(eq(HttpHeaders.AUTHORIZATION), contains("Bearer ")))
            .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenThrow(new RestClientException("boom"));
        when(tokenSupplier.get()).thenReturn("token");

        assertThatThrownBy(adapter::getConfigurations)
            .isInstanceOf(ProductConfigurationRetrievalException.class)
            .hasMessage("Impossible de recuperer la configuration produit")
            .hasCauseInstanceOf(RestClientException.class);
    }

    @Test
    void shouldThrowTimeoutSpecificRetrievalExceptionWhenConfiguratorReadTimesOut() {
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/v2/assurance-pret/versions-produit/offres/PROTEMP/actives"))
            .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(eq(HttpHeaders.AUTHORIZATION), contains("Bearer ")))
            .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenThrow(
            new ResourceAccessException("Read timed out", new SocketTimeoutException("Read timed out")));
        when(tokenSupplier.get()).thenReturn("token");

        assertThatThrownBy(adapter::getConfigurations)
            .isInstanceOf(ProductConfigurationRetrievalException.class)
            .hasMessage("Le service configurateur produit n'a pas repondu dans le delai imparti")
            .hasCauseInstanceOf(ResourceAccessException.class);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenResponseBodyIsNullOrEmpty() {
        mockRequest();
        when(responseSpec.body(RESPONSE_TYPE)).thenReturn(null);
        assertThatThrownBy(adapter::getConfigurations)
            .isInstanceOf(ProductConfigurationNotFoundException.class)
            .hasMessage("Configuration produit introuvable");

        when(responseSpec.body(RESPONSE_TYPE))
            .thenReturn(new ProductConfiguratorClientAdapter.ProductConfigurationResponseDto(null, null));
        assertThatThrownBy(adapter::getConfigurations)
            .isInstanceOf(ProductConfigurationNotFoundException.class);
    }

    @Test
    void shouldThrowNotFoundWhenResponseContainsNoProductCode() {
        var dto = product(null, null, "CI", null, List.of(), List.of());
        mockRestClientChain(new ProductConfiguratorClientAdapter.ProductConfigurationResponseDto(dto, null));

        assertThatThrownBy(adapter::getConfigurations)
            .isInstanceOf(ProductConfigurationNotFoundException.class);
    }

    @Test
    void shouldDeserializeAndMapRealCiAndCrdFixture() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ProductConfiguratorClientAdapter.ProductConfigurationResponseDto response =
            objectMapper.readValue(fixturePath().toFile(), RESPONSE_TYPE);

        ProductConfiguration ci = response.ci().toDomain();
        ProductConfiguration crd = response.crd().toDomain();

        assertThat(response.configurations()).hasSize(2);
        assertThat(ci.codeProduit()).isEqualTo("PROTEMPCI");
        assertThat(ci.modeCalcul()).isEqualTo("CI");
        assertThat(ci.dateEffetDebut()).isEqualTo(LocalDate.of(2026, 9, 29));
        assertThat(ci.garanties()).hasSize(8);
        assertThat(ci.detailConfig().territorialitesAdherent()).hasSize(3);
        assertThat(ci.detailConfig().modesFractionnement()).hasSize(4);
        assertThat(ci.garanties().get(2).franchises().get(0).exclusionLemoine()).isFalse();
        assertThat(ci.garanties().get(2).franchises().get(6).franchise().code()).isEqualTo("FR_60J");
        assertThat(ci.garanties().get(2).franchises().get(6).exclusionLemoine()).isTrue();
        assertThat(ci.garanties().get(2).franchises().get(7).franchise().code()).isEqualTo("FR_30J");
        assertThat(ci.garanties().get(2).franchises().get(7).exclusionLemoine()).isTrue();
        assertThat(ci.garanties().get(2).franchises().get(0).territorialiteExclusions()).isEmpty();
        assertThat(ci.coefficientsPassageFumeurCi()).hasSize(111);
        assertThat(ci.coefficientsAerasRefusCi()).hasSize(1440);
        assertThat(ci.mappingsCategoriePro()).hasSize(33);
        assertThat(ci.mappingsCategoriePro().get(1).classeRisqueDC().code()).isEqualTo("CSP4");
        assertThat(ci.mappingsCategoriePro().get(1).classeRisqueAT().code()).isEqualTo("CSP4");
        assertThat(ci.racEligibilites()).isEmpty();
        assertThat(ci.racCoefficients()).isEmpty();

        assertThat(crd.codeProduit()).isEqualTo("PROTEMPCRD");
        assertThat(crd.modeCalcul()).isEqualTo("CRD");
        assertThat(crd.garanties()).hasSize(8);
        assertThat(crd.courbeDeformationCrd()).hasSize(465);
        assertThat(crd.coefficientsPassageFumeurCrd()).hasSize(127);
        assertThat(crd.coefficientsAerasRefusCrd()).hasSize(54);
        assertThat(crd.typePretCourbesDeformationCrd()).hasSize(4);
    }

    @Test
    void shouldRejectInvalidConfigurationDateDuringDomainMapping() {
        var dto = product("PROD-001", "CODE-001", "CI", "not-a-date", List.of(), List.of());

        assertThatThrownBy(dto::toDomain).isInstanceOf(DateTimeException.class);
    }

    private void mockRestClientChain(
        ProductConfiguratorClientAdapter.ProductConfigurationResponseDto response
    ) {
        mockRequest();
        when(responseSpec.body(RESPONSE_TYPE)).thenReturn(response);
    }

    private void mockRequest() {
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/v2/assurance-pret/versions-produit/offres/PROTEMP/actives"))
            .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(eq(HttpHeaders.AUTHORIZATION), contains("Bearer ")))
            .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(tokenSupplier.get()).thenReturn("token");
    }

    private static Path fixturePath() {
        Path modulePath = Path.of("src", "main", "resources", "exemples", "configurateur",
            "reponse-config-produit-complet-CI_V2.json");
        return Files.exists(modulePath) ? modulePath : Path.of("app").resolve(modulePath);
    }

    private static ProductConfiguratorClientAdapter.ProductConfigV2Dto product(
        String codeProduit,
        String code,
        String modeCalcul,
        String dateEffetDebut,
        List<Map<String, Object>> racEligibilites,
        List<Map<String, Object>> racCoefficients
    ) {
        return new ProductConfiguratorClientAdapter.ProductConfigV2Dto(
            codeProduit,
            code,
            "Test Product",
            dateEffetDebut,
            null,
            "BROUILLON",
            modeCalcul,
            "LEMOINE_ET_HORS_LEMOINE",
            null,
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            racEligibilites,
            racCoefficients
        );
    }
}
