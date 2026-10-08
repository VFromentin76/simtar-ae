package fr.hm.tarificateur.domain.service;

import fr.hm.tarificateur.domain.exception.ProductConfigurationNotFoundException;
import fr.hm.tarificateur.domain.model.CoefficientPerimetreLemoineCi;
import fr.hm.tarificateur.domain.model.ConfigGarantie;
import fr.hm.tarificateur.domain.model.CoverageEndCoefficient;
import fr.hm.tarificateur.domain.model.Customer;
import fr.hm.tarificateur.domain.model.Loan;
import fr.hm.tarificateur.domain.model.PrimePureCi;
import fr.hm.tarificateur.domain.model.PrimePureCrd;
import fr.hm.tarificateur.domain.model.ProductConfiguration;
import fr.hm.tarificateur.domain.model.ProductQuote;
import fr.hm.tarificateur.domain.model.Quotation;
import fr.hm.tarificateur.domain.model.QuotationOptions;
import fr.hm.tarificateur.domain.model.QuotationResult;
import fr.hm.tarificateur.domain.model.Reference;
import fr.hm.tarificateur.domain.model.ScheduleLine;
import fr.hm.tarificateur.domain.model.Warranty;
import fr.hm.tarificateur.ports.outbound.ProductConfiguratorPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuotationServiceTest {

    @Mock
    private ProductConfiguratorPort productConfiguratorPort;

    @InjectMocks
    private QuotationService quotationService;

    private Warranty warranty;
    private Customer customer;
    private Loan loan;
    private QuotationOptions options;

    @BeforeEach
    void setUp() {
        lenient().when(productConfiguratorPort.getConfigurations()).thenReturn(defaultProductConfigurations());
        warranty = new Warranty(false, false, true, true, true, false, false, false, "100", "100");
        loan = new Loan("220000.00", "180", "1", "24", 1, "0.900", 1, 1, Map.of("1", warranty));
        options = new QuotationOptions("abbey national", true, "2025-06-01", Map.of("1", loan), 1, 7);
        customer = new Customer(
                "ASSUREE001", "test", null, 1, null, "ROUEN", "76000", "france", 1,
                "test un", "test", "", "1990-01-01", "", "", "FRANCE", 2, "0606060606",
                "", "", "test@example.com", 2, "dÃ©veloppeur", 90, false, false, false, 1,
                false, false, false, 0, false, false, false, 4010, null, "100", 50, true
        );
    }

    @Test
    void shouldCalculateQuotationWithSuccess() {
        Quotation quotation = new Quotation(
                "2025-02-15",
                Map.of("1", customer),
                options,
                "true",
                "HM",
                12
        );

        QuotationResult result = calculateFirstQuotation(quotation);

        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo("OK");
        assertThat(result.quotationId()).startsWith("Q-2025-02-15-");
        assertThat(result.customers()).hasSize(1);
        assertThat(result.customers().get(0).customerRef()).isEqualTo("ASSUREE001");
        assertThat(result.totals()).isNotNull();
        assertThat(result.totals().monthly()).isGreaterThan(0.0);
        assertThat(result.totals().annual()).isGreaterThan(0.0);
    }

    @Test
    void shouldRejectDromAndCorseSelectedTogether() {
        Warranty conflictingWarranty = new Warranty(
            false, false, true, true, true, false, false, false,
            "100", "100", false, null, true, true
        );
        Loan conflictingLoan = new Loan(
            "220000.00", "180", "1", "24", 1, "0.900", 1, 1,
            Map.of("1", conflictingWarranty)
        );
        Quotation quotation = new Quotation(
            "2025-02-15", Map.of("1", customer),
            new QuotationOptions("bank", false, "2025-06-01", Map.of("1", conflictingLoan), 1, 7),
            "true", "source", 12
        );

        assertThatThrownBy(() -> calculateFirstQuotation(quotation))
            .isInstanceOf(fr.hm.tarificateur.domain.exception.QuotationValidationException.class)
            .hasMessageContaining("DROM et Corse ne peuvent pas être sélectionnés ensemble");
        verifyNoInteractions(productConfiguratorPort);
    }

    @Test
    void shouldRejectIptCapitalOptionWithoutIptAndItt() {
        Warranty invalidWarranty = new Warranty(
            false, false, true, false, false, false, false, false,
            "100", "100", false, null, false, false, true
        );
        Loan invalidLoan = new Loan(
            "220000.00", "180", "1", "24", 1, "0.900", 1, 1,
            Map.of("1", invalidWarranty)
        );
        Quotation quotation = new Quotation(
            "2025-02-15", Map.of("1", customer),
            new QuotationOptions("bank", false, "2025-06-01", Map.of("1", invalidLoan), 1, 7),
            "true", "source", 12
        );

        assertThatThrownBy(() -> calculateFirstQuotation(quotation))
            .isInstanceOf(fr.hm.tarificateur.domain.exception.QuotationValidationException.class)
            .hasMessageContaining("nécessite les garanties IPT et ITT");
        verifyNoInteractions(productConfiguratorPort);
    }

    @Test
    void shouldStopWorkIncapacityWarrantyAfterCoverageEndAge() {
        Warranty ittWarranty = new Warranty(false, false, false, false, true, false, false, false, "100", "100");
        Loan scheduleLoan = new Loan("100000.00", "36", "1", "0", 1, "0.900", 1, 1, Map.of("1", ittWarranty));
        QuotationOptions scheduleOptions = new QuotationOptions(
            "bank", false, "2025-01-01", Map.of("1", scheduleLoan), 1, 7);
        ProductConfiguration configuration = new ProductConfiguration(
            "prod1", "prod1", "Produit", null, null, null, "CI", null, null,
            List.of(new ConfigGarantie(
                new Reference("ITT", "Incapacité temporaire de travail"), null, null, null, null,
                null, null, null, List.of(), List.of(), List.of(),
                List.of(new PrimePureCi(35, 3, 0.01)), List.of()
            )),
            List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(),
            List.of(), List.of(), List.of(new CoverageEndCoefficient(35, 36, 1.0))
        );
        when(productConfiguratorPort.getConfigurations()).thenReturn(configurationsWith(configuration));

        QuotationResult result = calculateFirstQuotation(new Quotation(
            "2025-01-01", Map.of("1", customer), scheduleOptions, "true", "source", 12
        ));

        assertThat(result.schedule()).extracting(ScheduleLine::year).contains(2025, 2026, 2027);
        assertThat(scheduleLine(result, 2026).itt()).isPositive();
        assertThat(scheduleLine(result, 2027).itt()).isZero();
        assertThat(scheduleLine(result, 2027).dcPtia()).isPositive();
    }

    @ParameterizedTest
    @CsvSource({"96, 2000.0, 2032", "100, 9000.0, 2033"})
    void shouldUseExactDurationInMonthsForScheduleAndRoundUpYearsForTariff(
        String durationMonths, double expectedPremium, int lastCoveredYear
    ) {
        Warranty noOptionalWarranties = new Warranty(false, false, false, false, false, false, false, false, "100", "100");
        Loan loanWithDuration = new Loan(
            "100000.00", durationMonths, "1", "0", 1, "0.900", 1, 1, Map.of("1", noOptionalWarranties));
        QuotationOptions durationOptions = new QuotationOptions(
            "bank", false, "2025-01-01", Map.of("1", loanWithDuration), 7, 7);
        ProductConfiguration configuration = new ProductConfiguration(
            "prod1", "prod1", "Produit", null, null, null, "CI", null, null,
            List.of(new ConfigGarantie(
                new Reference("DC", "Décès"), null, null, null, null,
                null, null, null, List.of(), List.of(), List.of(),
                List.of(new PrimePureCi(35, 8, 0.02), new PrimePureCi(35, 9, 0.09)), List.of()
            )),
            List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(),
            List.of(), List.of(), List.of(), List.of(), List.of(), List.of()
        );
        when(productConfiguratorPort.getConfigurations()).thenReturn(configurationsWith(configuration));

        QuotationResult result = calculateFirstQuotation(new Quotation(
            "2025-01-01", Map.of("1", customer), durationOptions, "true", "source", 12
        ));

        assertThat(result.customers().get(0).loans().get(0).premium()).isEqualTo(expectedPremium);
        assertThat(result.schedule()).extracting(ScheduleLine::year)
            .contains(lastCoveredYear).doesNotContain(lastCoveredYear + 1);
    }

    @Test
    void shouldSelectCoverageEndAgeMatchingExplicitAgeFinCouvertureOption() {
        Warranty ittWarranty = new Warranty(
            false, false, false, false, true, false, false, false,
            "100", "100", false, null, false, false, false, "AGE_70"
        );
        Loan scheduleLoan = new Loan("100000.00", "480", "1", "0", 1, "0.900", 1, 1, Map.of("1", ittWarranty));
        QuotationOptions scheduleOptions = new QuotationOptions(
            "bank", false, "2025-01-01", Map.of("1", scheduleLoan), 1, 7);
        ProductConfiguration configuration = new ProductConfiguration(
            "prod1", "prod1", "Produit", null, null, null, "CI", null, null,
            List.of(new ConfigGarantie(
                new Reference("ITT", "Incapacité temporaire de travail"), null, null, null, null,
                null, null, null, List.of(), List.of(), List.of(),
                List.of(new PrimePureCi(35, 40, 0.01)), List.of()
            )),
            List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(),
            List.of(), List.of(), List.of(
                new CoverageEndCoefficient(35, 65, 1.0),
                new CoverageEndCoefficient(35, 67, 1.0),
                new CoverageEndCoefficient(35, 70, 1.0)
            )
        );
        when(productConfiguratorPort.getConfigurations()).thenReturn(configurationsWith(configuration));

        QuotationResult result = calculateFirstQuotation(new Quotation(
            "2025-01-01", Map.of("1", customer), scheduleOptions, "true", "source", 12
        ));

        // Sans le choix explicite AGE_70, la sélection par âge le plus proche
        // aurait retenu AGE_65 (première entrée à égalité de distance) et la
        // garantie se serait arrêtée dès 2056.
        assertThat(scheduleLine(result, 2059).itt()).isPositive();
        assertThat(scheduleLine(result, 2061).itt()).isZero();
    }

    @Test
    void shouldKeepNearestAgeSelectionWhenAgeFinCouvertureOptionIsNotProvided() {
        Warranty ittWarranty = new Warranty(false, false, false, false, true, false, false, false, "100", "100");
        Loan scheduleLoan = new Loan("100000.00", "480", "1", "0", 1, "0.900", 1, 1, Map.of("1", ittWarranty));
        QuotationOptions scheduleOptions = new QuotationOptions(
            "bank", false, "2025-01-01", Map.of("1", scheduleLoan), 1, 7);
        ProductConfiguration configuration = new ProductConfiguration(
            "prod1", "prod1", "Produit", null, null, null, "CI", null, null,
            List.of(new ConfigGarantie(
                new Reference("ITT", "Incapacité temporaire de travail"), null, null, null, null,
                null, null, null, List.of(), List.of(), List.of(),
                List.of(new PrimePureCi(35, 40, 0.01)), List.of()
            )),
            List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(),
            List.of(), List.of(), List.of(
                new CoverageEndCoefficient(35, 65, 1.0),
                new CoverageEndCoefficient(35, 67, 1.0),
                new CoverageEndCoefficient(35, 70, 1.0)
            )
        );
        when(productConfiguratorPort.getConfigurations()).thenReturn(configurationsWith(configuration));

        QuotationResult result = calculateFirstQuotation(new Quotation(
            "2025-01-01", Map.of("1", customer), scheduleOptions, "true", "source", 12
        ));

        assertThat(scheduleLine(result, 2055).itt()).isPositive();
        assertThat(scheduleLine(result, 2056).itt()).isZero();
    }

    @Test
    void shouldCalculateQuotationWithProductConfiguratorPortSuccess() {
        Warranty warranty = new Warranty(false, false, true, true, true, false, false, false, "100", "100");
        Loan loan = new Loan("100000.00", "180", "1", "24", 1, "0.900", 1, 1, Map.of("1", warranty));
        QuotationOptions options = new QuotationOptions("abbey national", true, "2025-06-01", Map.of("1", loan), 1, 7);
        Customer customer = new Customer(
                "ASSUREE001", "test", null, 1, null, "ROUEN", "76000", "france", 1,
                "test un", "test", "", "1990-01-01", "", "", "FRANCE", 2, "0606060606",
                "", "", "test@example.com", 2, "dÃ©veloppeur", 90, false, false, false, 1,
                false, false, false, 0, false, false, false, 4010, null, "100", 50, true
        );

        Quotation quotation = new Quotation(
                "2025-02-15",
                Map.of("1", customer),
                options,
                "true",
                "HM",
                12
        );

        ProductConfiguration config = new ProductConfiguration(
                "prod1",           // codeProduit
                "prod1",           // code
                "Produit Sur-Mesure", // libelle
                null,              // dateEffetDebut
                null,              // dateEffetFin
                null,              // statut
                "CI",              // modeCalcul
                null,              // eligibiliteLemoine
                null,              // detailConfig
                List.of(new ConfigGarantie(
                        null,      // Reference garantie
                        null,      // Boolean booObligatoire
                        null,      // Integer ageAdhesionMin
                        null,      // Integer ageAdhesionMax
                        null,      // Reference territorialite
                        null,      // Double montantMensuelMaxIndemnisableEur
                        null,      // Double plafondCapitalMinEur
                        null,      // Double plafondCapitalMaxEur
                        List.of(), // List<Dependance> dependances
                        List.of(), // List<FranchiseCoefficient> franchises
                        List.of(), // List<OptionCoefficient> options
                        List.of(new PrimePureCi(35, 15, 0.0025)), // List<PrimePureCi> primesPuresCi
                        List.of(new PrimePureCrd(40, 0.0028))     // List<PrimePureCrd> primesPuresCrd
                )),
                List.of(),         // typePretEligibilites
                List.of(),         // objetPretEligibilites
                List.of(),         // typePretCoefficients
                List.of(),         // objetPretCoefficients
                List.of(),         // classeRisqueCoefficients
                List.of(),         // courbeDeformationCrd
                List.of(),         // coefficientsPassageFumeurCi
                List.of(),         // coefficientsPassageFumeurCrd
                List.of(),         // mappingsCategoriePro
                List.of()          // mappingsProfession
        );

        when(productConfiguratorPort.getConfigurations())
                .thenReturn(configurationsWith(config));

        QuotationResult result = calculateFirstQuotation(quotation);

        assertThat(result).isNotNull();
        assertThat(result.products()).singleElement().satisfies(product ->
                assertThat(product.name()).isEqualTo("Produit Sur-Mesure"));
        assertThat(result.totals()).isNotNull();

        verify(productConfiguratorPort).getConfigurations();
    }

    @Test
    void shouldThrowExceptionWhenQuotationIsNull() {
        assertThatThrownBy(() -> calculateFirstQuotation(null))
                .isInstanceOf(fr.hm.tarificateur.domain.exception.QuotationValidationException.class)
                .hasMessage("La requÃªte de cotation ne peut pas Ãªtre nulle");
        verifyNoInteractions(productConfiguratorPort);
    }

    @Test
    void shouldHandleQuotationWithoutCreationDate() {
        Quotation quotation = new Quotation(
                null,
                Map.of("1", customer),
                options,
                "true",
                "HM",
                12
        );

        QuotationResult result = calculateFirstQuotation(quotation);

        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo("OK");
        assertThat(result.customers()).hasSize(1);
    }

    @Test
    void shouldHandleQuotationWithCRDCalculationMode() {
        ProductConfiguration config = new ProductConfiguration(
                "prod1", "prod1", "Produit CRD", null, null, null, "CRD", null, null,
                List.of(new ConfigGarantie(
                        null, null, null, null, null, null, null, null,
                        List.of(), List.of(), List.of(),
                        List.of(),
                        List.of(new PrimePureCrd(35, 0.0020))
                )),
                List.of(), List.of(), List.of(), List.of(), List.of(), List.of(),
                List.of(), List.of(), List.of(), List.of()
        );

        when(productConfiguratorPort.getConfigurations())
                .thenReturn(configurationsWith(config));

        Quotation quotation = new Quotation(
                "2025-02-15",
                Map.of("1", customer),
                options,
                "true",
                "HM",
                12
        );

        QuotationResult result = quotationService.calculateQuotation(quotation).get(1);

        assertThat(result).isNotNull();
        assertThat(result.products()).singleElement().satisfies(product ->
                assertThat(product.code()).isEqualTo("prod1"));
    }

    @Test
    void shouldCalculateIndependentCiAndCrdQuotes() {
        ProductConfiguration ciConfiguration = buildProductConfiguration();
        ProductConfiguration crdConfiguration = buildCrdProductConfiguration("crd-product", 0.03);
        when(productConfiguratorPort.getConfigurations()).thenReturn(Map.of(
                "ci", ciConfiguration,
                "crd", crdConfiguration
        ));
        Quotation quotation = new Quotation(
                "2025-02-15", Map.of("1", customer), options, "true", "source", 12
        );

        List<QuotationResult> results = quotationService.calculateQuotation(quotation);

        assertThat(results).hasSize(2);
        QuotationResult ciResult = results.get(0);
        QuotationResult crdResult = results.get(1);
        assertThat(ciResult.products()).singleElement()
                .extracting(ProductQuote::code).isEqualTo("prod1");
        assertThat(crdResult.products()).singleElement()
                .extracting(ProductQuote::code).isEqualTo("crd-product");
        assertThat(ciResult.totals().annual()).isNotEqualTo(crdResult.totals().annual());
        assertThat(ciResult.schedule()).isNotEqualTo(crdResult.schedule());
    }

    @Test
    void shouldResolveProductsWithoutRequestIdentifiers() {
        Quotation quotation = new Quotation(
                "2025-02-15",
                Map.of("1", customer),
                options,
                "true",
                "HM",
                12
        );

        List<QuotationResult> results = quotationService.calculateQuotation(quotation);

        assertThat(results).hasSize(2);
        assertThat(results).allSatisfy(result ->
                assertThat(result.products()).hasSize(1));
    }

    @Test
    void shouldHandleQuotationWithoutCustomers() {
        Quotation quotation = new Quotation(
                "2025-02-15",
                null,
                options,
                "true",
                "HM",
                12
        );

        assertThatThrownBy(() -> calculateFirstQuotation(quotation))
                .isInstanceOf(fr.hm.tarificateur.domain.exception.QuotationValidationException.class)
                .hasMessage("Ã‰chec de la validation de la cotation : la liste des clients ne peut pas Ãªtre vide");
        verifyNoInteractions(productConfiguratorPort);
    }

    @Test
    void shouldHandleQuotationWithoutLoans() {
        QuotationOptions optionsWithoutLoans = new QuotationOptions(
                "abbey national", true, "2025-06-01", null, 1, 7
        );

        Quotation quotation = new Quotation(
                "2025-02-15",
                Map.of("1", customer),
                optionsWithoutLoans,
                "true",
                "HM",
                12
        );

        assertThatThrownBy(() -> calculateFirstQuotation(quotation))
                .isInstanceOf(fr.hm.tarificateur.domain.exception.QuotationValidationException.class)
                .hasMessage("Ã‰chec de la validation de la cotation : la liste des prÃªts ne peut pas Ãªtre vide");
        verifyNoInteractions(productConfiguratorPort);
    }

    @Test
    void shouldHandleCustomerWithoutPartnerRef() {
        Customer customerNoRef = new Customer(
                "CUST002", "test", null, 1, null, "PARIS", "75000", "france", 1,
                "test", "test", "", "1985-06-15", "", "", "FRANCE", 2, "0707070707",
                "", "", "test2@example.com", 2, "ingÃ©nieur", 85, false, false, false, 1,
                false, false, false, 0, false, false, false, 4010, null, "100", 50, true
        );

        Quotation quotation = new Quotation(
                "2025-02-15",
                Map.of("1", customerNoRef),
                options,
                "true",
                "HM",
                12
        );

        QuotationResult result = calculateFirstQuotation(quotation);

        assertThat(result.customers()).hasSize(1);
        assertThat(result.customers().get(0).customerRef()).isEqualTo("CUST002");
    }

    @Test
    void shouldHandleMultipleLoans() {
        Warranty warranty1 = new Warranty(true, false, false, true, false, false, false, false, null, null);
        Warranty warranty2 = new Warranty(false, true, true, false, false, false, false, false, null, null);
        Loan loan1 = new Loan("100000.00", "120", "1", "24", 1, "0.850", 1, 1, Map.of("W1", warranty1));
        Loan loan2 = new Loan("50000.00", "60", "2", "24", 1, "0.750", 1, 1, Map.of("W2", warranty2));

        QuotationOptions multiLoanOptions = new QuotationOptions(
                "abbey national", true, "2025-06-01",
                Map.of("L1", loan1, "L2", loan2), 1, 7
        );

        Quotation quotation = new Quotation(
                "2025-02-15",
                Map.of("1", customer),
                multiLoanOptions,
                "true",
                "HM",
                12
        );

        QuotationResult result = calculateFirstQuotation(quotation);

        assertThat(result.customers()).hasSize(1);
        assertThat(result.customers().get(0).loans()).hasSize(2);
        assertThat(result.totals().monthly()).isGreaterThan(0.0);
    }

    @Test
    void shouldRejectMissingProductConfigurations() {
        when(productConfiguratorPort.getConfigurations()).thenReturn(null);

        Quotation quotation = new Quotation(
                "2025-02-15",
                Map.of("1", customer),
                options,
                "true",
                "HM",
                12
        );

        assertThatThrownBy(() -> calculateFirstQuotation(quotation))
                .isInstanceOf(ProductConfigurationNotFoundException.class)
                .hasMessage("Configuration produit 'CI' introuvable");
    }

    @Test
    void shouldRejectMissingCiConfigurationEvenWhenCrdConfigurationExists() {
        when(productConfiguratorPort.getConfigurations()).thenReturn(Map.of(
                "crd", buildCrdProductConfiguration("prod-crd", 0.0020)
        ));

        Quotation quotation = new Quotation(
                "2025-02-15", Map.of("1", customer), options, "true", "HM", 12
        );

        assertThatThrownBy(() -> quotationService.calculateQuotation(quotation))
                .isInstanceOf(ProductConfigurationNotFoundException.class)
                .hasMessage("Configuration produit 'CI' introuvable");
    }

    @Test
    void shouldRejectMissingCrdConfigurationEvenWhenCiConfigurationExists() {
        when(productConfiguratorPort.getConfigurations()).thenReturn(Map.of(
                "ci", buildProductConfiguration()
        ));

        Quotation quotation = new Quotation(
                "2025-02-15", Map.of("1", customer), options, "true", "HM", 12
        );

        assertThatThrownBy(() -> quotationService.calculateQuotation(quotation))
                .isInstanceOf(ProductConfigurationNotFoundException.class)
                .hasMessage("Configuration produit 'CRD' introuvable");
    }

    @Test
    void shouldPropagateProductConfigurationNotFoundException() {
        when(productConfiguratorPort.getConfigurations())
            .thenThrow(new ProductConfigurationNotFoundException("prod1"));

        Quotation quotation = new Quotation(
            "2025-02-15",
            Map.of("1", customer),
            options,
            "true",
            "HM",
            12
        );

        assertThatThrownBy(() -> calculateFirstQuotation(quotation))
            .isInstanceOf(ProductConfigurationNotFoundException.class)
            .hasMessage("Configuration produit 'prod1' introuvable");
    }

    @Test
    void shouldFailWhenProductConfiguratorPortIsMissing() {
        quotationService = new QuotationService(null);

        Quotation quotation = new Quotation(
                "2025-02-15",
                Map.of("1", customer),
                options,
                "true",
                "HM",
                12
        );

        assertThatThrownBy(() -> calculateFirstQuotation(quotation))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldReturnFalseWhenReturnResultsIsFalse() {
        Quotation quotation = new Quotation(
                "2025-02-15",
                Map.of("1", customer),
                options,
                "false",
                "HM",
                12
        );

        QuotationResult result = calculateFirstQuotation(quotation);

        assertThat(result.returnResults()).isFalse();
    }

    @Test
    void shouldGenerateUniqueQuotationIds() {
        Quotation quotation1 = new Quotation(
                "2025-02-15",
                Map.of("1", customer),
                options,
                "true",
                "HM",
                12
        );

        Quotation quotation2 = new Quotation(
                "2025-02-15",
                Map.of("1", customer),
                options,
                "true",
                "HM",
                12
        );

        QuotationResult result1 = calculateFirstQuotation(quotation1);
        QuotationResult result2 = calculateFirstQuotation(quotation2);

        assertThat(result1.quotationId()).isNotEqualTo(result2.quotationId());
    }

    @Test
    void shouldHandleInvalidLoanAmountFallback() {
        Warranty warranty = new Warranty(false, false, true, true, false, false, false, false, null, null);
        Loan loan = new Loan("invalid", "120", "1", "24", 1, "0.900", 1, 1, Map.of("1", warranty));
        QuotationOptions optionsWithInvalidAmount = new QuotationOptions(
                "abbey national", true, "2025-06-01", Map.of("L1", loan), 1, 7
        );

        Quotation quotation = new Quotation(
                "2025-02-15",
                Map.of("1", customer),
                optionsWithInvalidAmount,
                "true",
                "HM",
                12
        );

        QuotationResult result = calculateFirstQuotation(quotation);

        assertThat(result).isNotNull();
        assertThat(result.customers()).hasSize(1);
    }

    @Test
    void shouldApplyLemoineMultiplierWhenAllEligibilityCriteriaAreMet() {
        ProductConfiguration config = buildProductConfiguration();
        Customer customerOutsideLemoineProfile = withDisclosedOverLemoineLimit(true);
        QuotationOptions lemoineOptions = withLoanDuration("180");

        Quotation quotationOutsideLemoineProfile = new Quotation(
                "2025-02-15", Map.of("1", customerOutsideLemoineProfile), lemoineOptions,
                "true", "HM", 12
        );
        Quotation quotationWithLemoineProfile = new Quotation(
                "2025-02-15", Map.of("1", customer), lemoineOptions,
                "true", "HM", 12
        );

        when(productConfiguratorPort.getConfigurations())
                .thenReturn(configurationsWith(config));

        QuotationResult resultOutsideLemoineProfile =
                calculateFirstQuotation(quotationOutsideLemoineProfile);
        QuotationResult resultWithLemoineProfile =
                calculateFirstQuotation(quotationWithLemoineProfile);

        assertThat(resultOutsideLemoineProfile.totals().monthly())
                .isLessThan(resultWithLemoineProfile.totals().monthly());
    }

    @Test
    void shouldCheckLemoineEligibilityUsingLoanDurationInMonths() {
        ProductConfiguration config = buildProductConfiguration();
        QuotationOptions optionsWithHundredMonthLoan = withLoanDuration("100");
        QuotationOptions realEstateOptions = new QuotationOptions(
            optionsWithHundredMonthLoan.bank(),
            optionsWithHundredMonthLoan.couple(),
            optionsWithHundredMonthLoan.effectiveDate(),
            optionsWithHundredMonthLoan.loans(),
            1,
            optionsWithHundredMonthLoan.projectState()
        );
        Quotation quotation = new Quotation(
            "2025-02-15", Map.of("1", customer), realEstateOptions,
            "true", "HM", 12
        );
        Quotation quotationOutsideLemoineProfile = new Quotation(
            "2025-02-15", Map.of("1", withDisclosedOverLemoineLimit(true)), realEstateOptions,
            "true", "HM", 12
        );
        when(productConfiguratorPort.getConfigurations()).thenReturn(configurationsWith(config));

        QuotationResult result = calculateFirstQuotation(quotation);
        QuotationResult resultOutsideLemoineProfile =
            calculateFirstQuotation(quotationOutsideLemoineProfile);

        assertThat(result.totals().monthly()).isGreaterThan(resultOutsideLemoineProfile.totals().monthly());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5, 9, 11})
    void shouldApplyLemoineMultiplierForRealEstateProjectQualifications(int projectQualification) {
        ProductConfiguration config = buildProductConfiguration();
        QuotationOptions realEstateOptions = withProjectQualification(projectQualification);
        Quotation quotation = new Quotation(
                "2025-02-15", Map.of("1", customer), realEstateOptions,
                "true", "HM", 12
        );
        Quotation quotationOutsideLemoineProfile = new Quotation(
                "2025-02-15", Map.of("1", withDisclosedOverLemoineLimit(true)), realEstateOptions,
                "true", "HM", 12
        );
        when(productConfiguratorPort.getConfigurations())
                .thenReturn(configurationsWith(config));

        QuotationResult result = calculateFirstQuotation(quotation);
        QuotationResult resultOutsideLemoineProfile =
                calculateFirstQuotation(quotationOutsideLemoineProfile);

        assertThat(result.totals().monthly())
                .isGreaterThan(resultOutsideLemoineProfile.totals().monthly());
    }

    @Test
    void shouldNotApplyLemoineMultiplierForConsumerLoan() {
        assertLemoineMultiplierIsNotApplied(customer, withProjectQualification(7));
    }

    @Test
    void shouldNotApplyLemoineMultiplierWhenCustomerIsSixtyAtLoanEnd() {
        Customer customerTurningSixtyAtLoanEnd = withBirthDate("1980-06-01");

        assertLemoineMultiplierIsNotApplied(customerTurningSixtyAtLoanEnd, withLoanDuration("180"));
    }

    private void assertLemoineMultiplierIsNotApplied(Customer testedCustomer, QuotationOptions testedOptions) {
        ProductConfiguration config = buildProductConfiguration();
        Quotation quotation = new Quotation(
                "2025-02-15", Map.of("1", testedCustomer), testedOptions,
                "true", "HM", 12
        );
        Quotation quotationOutsideLemoineProfile = new Quotation(
                "2025-02-15", Map.of("1", withDisclosedOverLemoineLimit(true, testedCustomer)), testedOptions,
                "true", "HM", 12
        );
        when(productConfiguratorPort.getConfigurations())
                .thenReturn(configurationsWith(config));

        QuotationResult result = calculateFirstQuotation(quotation);
        QuotationResult resultOutsideLemoineProfile =
                calculateFirstQuotation(quotationOutsideLemoineProfile);

        assertThat(result.totals().monthly())
                .isEqualTo(resultOutsideLemoineProfile.totals().monthly());
    }

    private ProductConfiguration buildProductConfiguration() {
        return new ProductConfiguration(
                "prod1", "prod1", "Produit Sur-Mesure", null, null, null, "CI",
                "HORS_LEMOINE", null,
                List.of(new ConfigGarantie(
                        null, null, null, null, null, null, null, null,
                        List.of(), List.of(), List.of(),
                        List.of(new PrimePureCi(35, 15, 0.0025)),
                        List.of(new PrimePureCrd(40, 0.0028))
                )),
                List.of(), List.of(), List.of(), List.of(), List.of(), List.of(),
                List.of(), List.of(), List.of(), List.of(), List.of(), List.of(),
                List.of(new CoefficientPerimetreLemoineCi(31, "VIE", 109.5))
        );
    }

    private ProductConfiguration buildCrdProductConfiguration(String code, double annualRate) {
        return new ProductConfiguration(
                code, code, "Produit CRD", null, null, null, "CRD", null, null,
                List.of(new ConfigGarantie(
                        null, null, null, null, null, null, null, null,
                        List.of(), List.of(), List.of(), List.of(),
                        List.of(new PrimePureCrd(35, annualRate))
                )),
                List.of(), List.of(), List.of(), List.of(), List.of(), List.of(),
                List.of(), List.of(), List.of(), List.of()
        );
    }

    private Map<String, ProductConfiguration> defaultProductConfigurations() {
        return Map.of(
                "ci", buildProductConfiguration(),
                "crd", buildCrdProductConfiguration("prod-crd", 0.0020)
        );
    }

    private Map<String, ProductConfiguration> configurationsWith(ProductConfiguration configuration) {
        ProductConfiguration ci = "CI".equalsIgnoreCase(configuration.modeCalcul())
                ? configuration
                : buildProductConfiguration();
        ProductConfiguration crd = "CRD".equalsIgnoreCase(configuration.modeCalcul())
                ? configuration
                : buildCrdProductConfiguration("prod-crd", 0.0020);
        return Map.of("ci", ci, "crd", crd);
    }

    private QuotationResult calculateFirstQuotation(Quotation quotation) {
        return quotationService.calculateQuotation(quotation).get(0);
    }

    private ScheduleLine scheduleLine(QuotationResult result, int year) {
        return result.schedule().stream()
            .filter(line -> line.year() == year)
            .findFirst()
            .orElseThrow();
    }

    private Customer withDisclosedOverLemoineLimit(boolean disclosedOverLemoineLimit) {
        return withDisclosedOverLemoineLimit(disclosedOverLemoineLimit, customer);
    }

    private Customer withDisclosedOverLemoineLimit(boolean disclosedOverLemoineLimit, Customer source) {
        return new Customer(
                source.partnerCustomerRef(), source.address(), source.beneficiaries(),
                source.beneficiaryClause(), source.beneficiaryType(), source.city(),
                source.zipCode(), source.country(), source.civility(), source.firstname(),
                source.lastname(), source.maidenName(), source.birthDate(), source.birthZipcode(),
                source.birthCity(), source.birthCountry(), source.familySituation(), source.cellPhone(),
                source.homePhone(), source.officePhone(), source.email(), source.profession(),
                source.exactProfession(), source.franchise(), source.handling(), source.height(),
                source.businessTrip(), source.isMainCustomer(), source.partTime(), source.riskyProfession(),
                source.riskySport(), source.riskyProfessionId(), source.smoker(), disclosedOverLemoineLimit,
                source.travelingAbroad(), source.modulation(), source.nextAddress(), source.fees(),
                source.brokerFees(), source.isBrokerFeesSpread()
        );
    }

    private Customer withBirthDate(String birthDate) {
        Customer source = customer;
        return new Customer(
                source.partnerCustomerRef(), source.address(), source.beneficiaries(),
                source.beneficiaryClause(), source.beneficiaryType(), source.city(),
                source.zipCode(), source.country(), source.civility(), source.firstname(),
                source.lastname(), source.maidenName(), birthDate, source.birthZipcode(),
                source.birthCity(), source.birthCountry(), source.familySituation(), source.cellPhone(),
                source.homePhone(), source.officePhone(), source.email(), source.profession(),
                source.exactProfession(), source.franchise(), source.handling(), source.height(),
                source.businessTrip(), source.isMainCustomer(), source.partTime(), source.riskyProfession(),
                source.riskySport(), source.riskyProfessionId(), source.smoker(), source.disclosedOverLemoineLimit(),
                source.travelingAbroad(), source.modulation(), source.nextAddress(), source.fees(),
                source.brokerFees(), source.isBrokerFeesSpread()
        );
    }

    private QuotationOptions withProjectQualification(int projectQualification) {
        QuotationOptions source = withLoanDuration("180");
        return new QuotationOptions(
                source.bank(), source.couple(), source.effectiveDate(), source.loans(),
                projectQualification, source.projectState()
        );
    }

    private QuotationOptions withLoanDuration(String duration) {
        Loan source = loan;
        Loan loanWithDuration = new Loan(
                source.amount(), duration, source.delayType(), source.delay(), source.partnerLoanRef(),
                source.rate(), source.rateType(), source.type(), source.warranties()
        );
        return new QuotationOptions(
                options.bank(), options.couple(), options.effectiveDate(), Map.of("1", loanWithDuration),
                options.projectQualification(), options.projectState()
        );
    }

    @Test
    void shouldHandleNoWarrantiesCase() {
        Loan loanNoWarranties = new Loan("100000.00", "120", "1", "24", 1, "0.900", 1, 1, new HashMap<>());
        QuotationOptions optionsNoWarranties = new QuotationOptions(
                "abbey national", true, "2025-06-01", Map.of("L1", loanNoWarranties), 1, 7
        );

        Quotation quotation = new Quotation(
                "2025-02-15",
                Map.of("1", customer),
                optionsNoWarranties,
                "true",
                "HM",
                12
        );

        QuotationResult result = calculateFirstQuotation(quotation);

        assertThat(result).isNotNull();
        assertThat(result.totals().monthly()).isGreaterThan(0.0);
    }
}
