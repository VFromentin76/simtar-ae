package fr.hm.tarificateur.domain.service;

import fr.hm.tarificateur.domain.model.ClasseRisqueCoefficient;
import fr.hm.tarificateur.domain.model.CoefficientPassageFumeurCi;
import fr.hm.tarificateur.domain.model.CoefficientPerimetreLemoineCi;
import fr.hm.tarificateur.domain.model.ConfigGarantie;
import fr.hm.tarificateur.domain.model.CoverageEndCoefficient;
import fr.hm.tarificateur.domain.model.Dependance;
import fr.hm.tarificateur.domain.model.DetailConfig;
import fr.hm.tarificateur.domain.model.FranchiseCoefficient;
import fr.hm.tarificateur.domain.model.ObjetPretCoefficient;
import fr.hm.tarificateur.domain.model.OptionCoefficient;
import fr.hm.tarificateur.domain.model.PrimePureCi;
import fr.hm.tarificateur.domain.model.PrimePureCrd;
import fr.hm.tarificateur.domain.model.ProfessionClasseRisqueMapping;
import fr.hm.tarificateur.domain.model.Reference;
import fr.hm.tarificateur.domain.model.TerritorialiteCoefficient;
import fr.hm.tarificateur.domain.model.TypePretCoefficient;
import fr.hm.tarificateur.domain.model.Warranty;
import fr.hm.tarificateur.domain.model.WarrantyPricingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

class WarrantyCalculationServiceTest {

    private WarrantyCalculationService warrantyCalculationService;
    private List<ConfigGarantie> configGaranties;
    private static final double RESOLVED_QUOTITY_FACTOR = 1.0;

    @BeforeEach
    void setUp() {
        warrantyCalculationService = new WarrantyCalculationService();
        configGaranties = buildSampleConfigGaranties();
    }

    /**
     * Test principal: Calcul multi-garanties (IP + ITT)
     */
    @Test
    void shouldCalculatePremiumsForMultipleSubscribedWarranties() {
        // Arrange
        Warranty warranty = new Warranty(true, false, false, false, true, false, false, false, "100%", "100%");
        Integer ageAdhesion = 45;
        Integer durationYears = 20;
        Double loanAmount = 200000.0;

        // Act
        Map<String, Double> premiums = warrantyCalculationService.calculerPrimesPuresDesGaranties(
                warranty,
                configGaranties,
                ageAdhesion,
                durationYears,
                loanAmount,
                RESOLVED_QUOTITY_FACTOR);

        // Assert
        assertThat(premiums).isNotEmpty();
        assertThat(premiums).containsKeys("IP", "ITT");
        
        // IP: 200000 * 0.0005 = 100.00€
        assertThat(premiums.get("IP")).isCloseTo(100.0, within(0.1));
        
        // ITT: 200000 * 0.00085 = 170.00€
        assertThat(premiums.get("ITT")).isCloseTo(170.0, within(0.1));
    }

    @Test
    void shouldApplySelectedFranchiseCoefficientToNonLifeWarranty() {
        ConfigGarantie ip = new ConfigGarantie(
            new Reference("IP", "Invalidité Permanente"),
            false, 18, 65, null, null, null, null,
            Collections.emptyList(),
            List.of(new FranchiseCoefficient(new Reference("FR_60J", "Franchise 60 jours"), false, 125.0)),
            Collections.emptyList(),
            List.of(new PrimePureCi(45, 20, 0.0005)),
            Collections.emptyList()
        );
        Warranty warranty = new Warranty(true, false, false, false, false, false, false, false, "100%", "100%");
        WarrantyPricingContext context = contextWith(
            null, null, null, false, null, null, null, null,
            null, null, null, null, null, "FR_60J"
        );

        Map<String, Double> premiums = warrantyCalculationService.calculerPrimesPuresDesGaranties(
            warranty, List.of(ip), 45, 20, 200000.0, 1.0, context);

        assertThat(premiums.get("IP")).isCloseTo(125.0, within(0.01));
    }

    @Test
    void shouldApplyMnoOptionCoefficientOnlyToTargetedNonLifeWarranty() {
        ConfigGarantie dos = new ConfigGarantie(
            new Reference("DOS", "Dorsalgies"),
            false, 18, 65, null, null, null, null,
            Collections.emptyList(), Collections.emptyList(),
            List.of(new OptionCoefficient(new Reference("DOS_RACHAT_1", "Rachat MNO 1"), false, 130.0)),
            List.of(new PrimePureCi(45, 20, 0.0005)), Collections.emptyList()
        );
        ConfigGarantie mno = new ConfigGarantie(
            new Reference("MNO", "Rachat MNO"),
            false, 18, 65, null, null, null, null,
            Collections.emptyList(), Collections.emptyList(),
            List.of(new OptionCoefficient(new Reference("DOS_RACHAT_1", "Rachat MNO 1"), false, 130.0)),
            Collections.emptyList(), Collections.emptyList()
        );
        Warranty warranty = new Warranty(
            false, false, false, false, false, true, false, false,
            "100%", "100%", true, "DOS_RACHAT_1"
        );
        WarrantyPricingContext context = new WarrantyPricingContext(
            null, null, null, false, null, null, null, null,
            null, null, null, null, null, null, null, "DOS_RACHAT_1"
        );

        Map<String, Double> premiums = warrantyCalculationService.calculerPrimesPuresDesGaranties(
            warranty, List.of(dos, mno), 45, 20, 200000.0, 1.0, context);

        assertThat(premiums.get("DOS")).isCloseTo(130.0, within(0.01));
        assertThat(premiums).doesNotContainKey("MNO");
    }

    @Test
    void shouldRejectMnoWhenConfiguredDependencyIsMissing() {
        ConfigGarantie mno = new ConfigGarantie(
            new Reference("MNO", "Rachat MNO"),
            false, 18, 65, null, null, null, null,
            List.of(new Dependance(new Reference("ITT", "Incapacité Temporaire Totale"))),
            Collections.emptyList(), Collections.emptyList(),
            Collections.emptyList(), Collections.emptyList()
        );
        Warranty warranty = new Warranty(
            false, false, false, false, false, false, false, false,
            "100%", "100%", true, "DOS_RACHAT_1"
        );

        assertThatThrownBy(() -> warrantyCalculationService.calculerPrimesPuresDesGaranties(
            warranty, List.of(mno), 45, 20, 200000.0, 1.0, WarrantyPricingContext.empty()))
            .isInstanceOf(fr.hm.tarificateur.domain.exception.QuotationValidationException.class)
            .hasMessageContaining("ITT n'est pas souscrite");
    }

    @Test
    void shouldRejectMnoWhenConfigurationIsMissing() {
        ConfigGarantie itt = new ConfigGarantie(
            new Reference("ITT", "Incapacité Temporaire Totale"),
            false, 18, 65, null, null, null, null,
            Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
            List.of(new PrimePureCi(45, 20, 0.0005)), Collections.emptyList()
        );
        Warranty warranty = new Warranty(
            false, false, false, false, true, false, false, false,
            "100%", "100%", true, "DOS_RACHAT_1"
        );

        assertThatThrownBy(() -> warrantyCalculationService.calculerPrimesPuresDesGaranties(
            warranty, List.of(itt), 45, 20, 200000.0, 1.0, WarrantyPricingContext.empty()))
            .isInstanceOf(fr.hm.tarificateur.domain.exception.QuotationValidationException.class)
            .hasMessageContaining("configuration MNO est absente");
    }

    @Test
    void shouldApplyNeutralDromMultiplierWhenNoTerritorialiteConfigurationFound() {
        ConfigGarantie ip = new ConfigGarantie(
            new Reference("IP", "Invalidité Permanente"),
            false, 18, 65, null, null, null, null,
            Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
            List.of(new PrimePureCi(45, 20, 0.0005)), Collections.emptyList()
        );
        Warranty warranty = new Warranty(
            true, false, false, false, false, false, false, false,
            "100%", "100%", false, null, true, false
        );
        WarrantyPricingContext context = new WarrantyPricingContext(
            null, null, null, false, null, null, null, null,
            null, null, null, null, null, null, null, null, true, false, null, null, null, null
        );

        Map<String, Double> premiums = warrantyCalculationService.calculerPrimesPuresDesGaranties(
            warranty, List.of(ip), 45, 20, 200000.0, 1.0, context);

        assertThat(premiums.get("IP")).isCloseTo(100.0, within(0.01));
    }

    @Test
    void shouldApplyRealTerritorialityCoefficientFromProductConfiguration() {
        ConfigGarantie ip = new ConfigGarantie(
            new Reference("IP", "Invalidité Permanente"),
            false, 18, 65, null, null, null, null,
            Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
            List.of(new PrimePureCi(45, 20, 0.0005)), Collections.emptyList()
        );
        Warranty warranty = new Warranty(
            true, false, false, false, false, false, false, false,
            "100%", "100%", false, null, true, false
        );
        List<TerritorialiteCoefficient> territorialiteCoefficients = List.of(
            new TerritorialiteCoefficient(new Reference("DROM", "DROM Mayotte incluse"), 120.0),
            new TerritorialiteCoefficient(new Reference("DROM_HORS_MAYOTTE", "DROM hors Mayotte"), 110.0),
            new TerritorialiteCoefficient(new Reference("CORSE", "Corse"), 105.0)
        );
        WarrantyPricingContext context = new WarrantyPricingContext(
            null, null, null, false, null, null, null, null,
            null, null, null, null, null, null, null, null, true, false, null, null, null,
            territorialiteCoefficients
        );

        Map<String, Double> premiums = warrantyCalculationService.calculerPrimesPuresDesGaranties(
            warranty, List.of(ip), 45, 20, 200000.0, 1.0, context);

        assertThat(premiums.get("IP")).isCloseTo(120.0, within(0.01));
    }

    @Test
    void shouldApplyRealCorseCoefficientFromProductConfiguration() {
        ConfigGarantie ip = new ConfigGarantie(
            new Reference("IP", "Invalidité Permanente"),
            false, 18, 65, null, null, null, null,
            Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
            List.of(new PrimePureCi(45, 20, 0.0005)), Collections.emptyList()
        );
        Warranty warranty = new Warranty(
            true, false, false, false, false, false, false, false,
            "100%", "100%", false, null, false, true
        );
        List<TerritorialiteCoefficient> territorialiteCoefficients = List.of(
            new TerritorialiteCoefficient(new Reference("DROM", "DROM Mayotte incluse"), 120.0),
            new TerritorialiteCoefficient(new Reference("CORSE", "Corse"), 105.0)
        );
        WarrantyPricingContext context = new WarrantyPricingContext(
            null, null, null, false, null, null, null, null,
            null, null, null, null, null, null, null, null, false, true, null, null, null,
            territorialiteCoefficients
        );

        Map<String, Double> premiums = warrantyCalculationService.calculerPrimesPuresDesGaranties(
            warranty, List.of(ip), 45, 20, 200000.0, 1.0, context);

        assertThat(premiums.get("IP")).isCloseTo(105.0, within(0.01));
    }

    @Test
    void shouldApplyIptCapitalOptionToIptAndItt() {
        ConfigGarantie ipt = new ConfigGarantie(
            new Reference("IPT", "Invalidité Permanente Totale"),
            false, 18, 65, null, null, null, null,
            Collections.emptyList(), Collections.emptyList(),
            List.of(new OptionCoefficient(
                new Reference("IPT_SORTIE_CAPITAL", "Sortie en capital"), false, 112.5)),
            List.of(new PrimePureCi(45, 20, 0.0005)), Collections.emptyList()
        );
        ConfigGarantie itt = new ConfigGarantie(
            new Reference("ITT", "Incapacité Temporaire Totale"),
            false, 18, 65, null, null, null, null,
            Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
            List.of(new PrimePureCi(45, 20, 0.0005)), Collections.emptyList()
        );
        Warranty warranty = new Warranty(
            false, false, true, false, true, false, false, false,
            "100%", "100%", false, null, false, false, true
        );
        WarrantyPricingContext context = new WarrantyPricingContext(
            null, null, null, false, null, null, null, null,
            null, null, null, null, null, null, null, null, false, false, true, null, null, null
        );

        Map<String, Double> premiums = warrantyCalculationService.calculerPrimesPuresDesGaranties(
            warranty, List.of(ipt, itt), 45, 20, 200000.0, 1.0, context);

        assertThat(premiums.get("IPT")).isCloseTo(112.5, within(0.01));
        assertThat(premiums.get("ITT")).isCloseTo(112.5, within(0.01));
    }

    @Test
    void shouldApplyCoverageEndCoefficientWhenAgeFinCouvertureOptionIsSelected() {
        ConfigGarantie ip = new ConfigGarantie(
            new Reference("IP", "Invalidité Permanente"),
            false, 18, 65, null, null, null, null,
            Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
            List.of(new PrimePureCi(45, 20, 0.0005)), Collections.emptyList()
        );
        Warranty warranty = new Warranty(
            true, false, false, false, false, false, false, false,
            "100%", "100%", false, null, false, false, false, "AGE_67"
        );
        List<CoverageEndCoefficient> coverageEndCoefficients = List.of(
            new CoverageEndCoefficient(45, 65, 1.0),
            new CoverageEndCoefficient(45, 67, 1.1),
            new CoverageEndCoefficient(45, 70, 1.2)
        );
        WarrantyPricingContext context = new WarrantyPricingContext(
            null, null, null, false, null, null, null, null,
            null, null, null, null, null, null, null, null, false, false, null,
            "AGE_67", coverageEndCoefficients, null
        );

        Map<String, Double> premiums = warrantyCalculationService.calculerPrimesPuresDesGaranties(
            warranty, List.of(ip), 45, 20, 200000.0, 1.0, context);

        assertThat(premiums.get("IP")).isCloseTo(110.0, within(0.01));
    }

    @Test
    void shouldNotApplyCoverageEndCoefficientWhenOptionIsNotSelected() {
        ConfigGarantie ip = new ConfigGarantie(
            new Reference("IP", "Invalidité Permanente"),
            false, 18, 65, null, null, null, null,
            Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
            List.of(new PrimePureCi(45, 20, 0.0005)), Collections.emptyList()
        );
        Warranty warranty = new Warranty(
            true, false, false, false, false, false, false, false, "100%", "100%"
        );
        List<CoverageEndCoefficient> coverageEndCoefficients = List.of(
            new CoverageEndCoefficient(45, 65, 1.0),
            new CoverageEndCoefficient(45, 67, 1.1),
            new CoverageEndCoefficient(45, 70, 1.2)
        );
        WarrantyPricingContext context = new WarrantyPricingContext(
            null, null, null, false, null, null, null, null,
            null, null, null, null, null, null, null, null, false, false, null,
            null, coverageEndCoefficients, null
        );

        Map<String, Double> premiums = warrantyCalculationService.calculerPrimesPuresDesGaranties(
            warranty, List.of(ip), 45, 20, 200000.0, 1.0, context);

        assertThat(premiums.get("IP")).isCloseTo(100.0, within(0.01));
    }

    /**
     * Test: Garantie non souscrite n'est pas incluse
     */
    @Test
    void shouldNotIncludeUnsubscribedWarranties() {
        // Arrange
        Warranty warranty = new Warranty(false, false, false, false, true, false, false, false, "100%", "100%");
        Integer ageAdhesion = 45;
        Integer durationYears = 20;
        Double loanAmount = 200000.0;

        // Act
        Map<String, Double> premiums = warrantyCalculationService.calculerPrimesPuresDesGaranties(
                warranty,
                configGaranties,
                ageAdhesion,
                durationYears,
                loanAmount,
                RESOLVED_QUOTITY_FACTOR);

        // Assert
        assertThat(premiums).doesNotContainKey("IP");
        assertThat(premiums).containsKey("ITT");
    }

    /**
     * Test: Garantie avec dÃ©pendance non satisfaite est inÃ©ligible
     */
    @Test
    void shouldReturnZeroPremiumWhenDependencyNotSatisfied() {
        // Arrange
        // IPP dÃ©pend de IP, mais IP n'est pas souscrit
        Warranty warranty = new Warranty(false, true, false, false, false, false, false, false, "100%", "100%");
        Integer ageAdhesion = 45;
        Integer durationYears = 20;
        Double loanAmount = 200000.0;

        // Act
        Map<String, Double> premiums = warrantyCalculationService.calculerPrimesPuresDesGaranties(
                warranty,
                configGaranties,
                ageAdhesion,
                durationYears,
                loanAmount,
                RESOLVED_QUOTITY_FACTOR);

        // Assert
        assertThat(premiums.getOrDefault("IPP", 0.0)).isEqualTo(0.0);
    }

    /**
     * Test: Ã‚ge < minimum d'adhÃ©sion rend inÃ©ligible
     */
    @Test
    void shouldReturnZeroPremiumWhenAgeUnderMinimum() {
        // Arrange
        Warranty warranty = new Warranty(true, false, false, false, false, false, false, false, "100%", "100%");
        Integer ageAdhesion = 15;  // Minimum est 18
        Integer durationYears = 20;
        Double loanAmount = 200000.0;

        // Act
        Map<String, Double> premiums = warrantyCalculationService.calculerPrimesPuresDesGaranties(
                warranty,
                configGaranties,
                ageAdhesion,
                durationYears,
                loanAmount,
                RESOLVED_QUOTITY_FACTOR);

        // Assert
        assertThat(premiums.getOrDefault("IP", 0.0)).isEqualTo(0.0);
    }

    /**
     * Test: Ã‚ge > maximum d'adhÃ©sion rend inÃ©ligible
     */
    @Test
    void shouldReturnZeroPremiumWhenAgeOverMaximum() {
        // Arrange
        Warranty warranty = new Warranty(true, false, false, false, false, false, false, false, "100%", "100%");
        Integer ageAdhesion = 70;  // Maximum est 65
        Integer durationYears = 20;
        Double loanAmount = 200000.0;

        // Act
        Map<String, Double> premiums = warrantyCalculationService.calculerPrimesPuresDesGaranties(
                warranty,
                configGaranties,
                ageAdhesion,
                durationYears,
                loanAmount,
                RESOLVED_QUOTITY_FACTOR);

        // Assert
        assertThat(premiums.getOrDefault("IP", 0.0)).isEqualTo(0.0);
    }

    /**
     * Test: Coefficient exact trouvÃ©
     */
    @Test
    void shouldUseExactCoefficientWhenAvailable() {
        // Arrange
        Warranty warranty = new Warranty(true, false, false, false, false, false, false, false, "100%", "100%");
        Integer ageAdhesion = 45;
        Integer durationYears = 20;
        Double loanAmount = 100000.0;

        // Act
        Map<String, Double> premiums = warrantyCalculationService.calculerPrimesPuresDesGaranties(
                warranty,
                configGaranties,
                ageAdhesion,
                durationYears,
                loanAmount,
                RESOLVED_QUOTITY_FACTOR);

        // Assert
        // IP exact (45, 20) = 0.0005 → 100000 * 0.0005 = 50.00€
        assertThat(premiums.get("IP")).isCloseTo(50.0, within(0.1));
    }

    /**
     * Test: Fallback sur coefficient le plus proche
     */
    @Test
    void shouldUseFallbackClosestCoefficientWhenExactNotFound() {
        // Arrange
        Warranty warranty = new Warranty(true, false, false, false, false, false, false, false, "100%", "100%");
        Integer ageAdhesion = 50;  // Pas d'exact (45, 20) mais il doit prendre le plus proche
        Integer durationYears = 20;
        Double loanAmount = 100000.0;

        // Act
        Map<String, Double> premiums = warrantyCalculationService.calculerPrimesPuresDesGaranties(
                warranty,
                configGaranties,
                ageAdhesion,
                durationYears,
                loanAmount,
                RESOLVED_QUOTITY_FACTOR);

        // Assert
        // Ne doit pas Ãªtre 0
        assertThat(premiums.get("IP")).isGreaterThan(0.0);
    }

    /**
     * Test: Null warranty retourne map vide
     */
    @Test
    void shouldReturnEmptyMapWhenWarrantyIsNull() {
        // Act
        Map<String, Double> premiums = warrantyCalculationService.calculerPrimesPuresDesGaranties(
                null,
                configGaranties,
                45,
                20,
                200000.0,
                RESOLVED_QUOTITY_FACTOR);

        // Assert
        assertThat(premiums).isEmpty();
    }

    /**
     * Test: Null configGaranties retourne map vide
     */
    @Test
    void shouldReturnEmptyMapWhenConfigGarantiesIsNull() {
        // Arrange
        Warranty warranty = new Warranty(true, false, false, false, false, false, false, false, "100%", "100%");

        // Act
        Map<String, Double> premiums = warrantyCalculationService.calculerPrimesPuresDesGaranties(
                warranty,
                null,
                45,
                20,
                200000.0,
                RESOLVED_QUOTITY_FACTOR);

        // Assert
        assertThat(premiums).isEmpty();
    }

    /**
     * Test: Garantie souscrite mais non trouvÃ©e dans config retourne 0
     */
    @Test
    void shouldReturnZeroPremiumWhenWarrantyNotInConfig() {
        // Arrange
        Warranty warranty = new Warranty(true, false, false, false, false, false, false, false, "100%", "100%");
        List<ConfigGarantie> emptyConfig = new ArrayList<>();

        // Act
        Map<String, Double> premiums = warrantyCalculationService.calculerPrimesPuresDesGaranties(
                warranty,
                emptyConfig,
                45,
                20,
                200000.0,
                RESOLVED_QUOTITY_FACTOR);

        // Assert
        assertThat(premiums.getOrDefault("IP", 0.0)).isEqualTo(0.0);
    }

    /**
     * Test: Calcul correct pour montants diffÃ©rents
     */
    @Test
    void shouldCalculateProportionallyToLoanAmount() {
        // Arrange
        Warranty warranty = new Warranty(true, false, false, false, false, false, false, false, "100%", "100%");
        Integer ageAdhesion = 45;
        Integer durationYears = 20;

        // Act
        Map<String, Double> premiums100k = warrantyCalculationService.calculerPrimesPuresDesGaranties(
                warranty,
                configGaranties,
                ageAdhesion,
                durationYears,
                100000.0,
                RESOLVED_QUOTITY_FACTOR);

        Map<String, Double> premiums200k = warrantyCalculationService.calculerPrimesPuresDesGaranties(
                warranty,
                configGaranties,
                ageAdhesion,
                durationYears,
                200000.0,
                RESOLVED_QUOTITY_FACTOR);

        // Assert
        // 200k doit Ãªtre ~2x plus que 100k
        assertThat(premiums200k.get("IP")).isCloseTo(premiums100k.get("IP") * 2, within(0.1));
    }

    /**
     * Test: Toutes les garanties avec tous les boolÃ©ens Ã  true
     */
    @Test
    void shouldHandleAllWarrantiesSubscribed() {
        // Arrange
        Warranty warranty = new Warranty(true, true, true, true, true, true, true, true, "100%", "100%");
        Integer ageAdhesion = 45;
        Integer durationYears = 20;
        Double loanAmount = 100000.0;

        // Act
        Map<String, Double> premiums = warrantyCalculationService.calculerPrimesPuresDesGaranties(
                warranty,
                configGaranties,
                ageAdhesion,
                durationYears,
                loanAmount,
                RESOLVED_QUOTITY_FACTOR);

        // Assert
        // Au moins IP et ITT doivent Ãªtre prÃ©sents
        assertThat(premiums).containsKeys("IP", "ITT");
        // IPP dÃ©pend de IP donc doit Ãªtre incluse
        assertThat(premiums).containsKey("IPP");
    }

    /**
     * Test: Garantie avec primesPuresCi null
     */
    @Test
    void shouldReturnZeroPremiumWhenPrimesPuresCiIsNull() {
        // Arrange
        ConfigGarantie configNoPrimes = new ConfigGarantie(
                new Reference("IP", "InvaliditÃ© Permanente"),
                false,
                18,
                65,
                null,
                null,
                null,
                null,
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                null,  // primesPuresCi = null
                Collections.emptyList()
        );

        List<ConfigGarantie> configWithNullPrimes = List.of(configNoPrimes);
        Warranty warranty = new Warranty(true, false, false, false, false, false, false, false, "100%", "100%");

        // Act
        Map<String, Double> premiums = warrantyCalculationService.calculerPrimesPuresDesGaranties(
                warranty,
                configWithNullPrimes,
                45,
                20,
                200000.0,
                RESOLVED_QUOTITY_FACTOR);

        // Assert
        assertThat(premiums.getOrDefault("IP", 0.0)).isEqualTo(0.0);
    }

    /**
     * Test: Aucune garantie souscrite = map vide
     */
    @Test
    void shouldReturnEmptyMapWhenNoWarrantiesSubscribed() {
        // Arrange
        Warranty warranty = new Warranty(false, false, false, false, false, false, false, false, "100%", "100%");

        // Act
        Map<String, Double> premiums = warrantyCalculationService.calculerPrimesPuresDesGaranties(
                warranty,
                configGaranties,
                45,
                20,
                200000.0,
                RESOLVED_QUOTITY_FACTOR);

        // Assert
        assertThat(premiums).isEmpty();
    }

    /**
     * Test: Rounding Ã  2 dÃ©cimales
     */
    @Test
    void shouldRoundPremiumsTo2Decimals() {
        // Arrange
        Warranty warranty = new Warranty(true, false, false, false, false, false, false, false, "100%", "100%");
        // 200000 * 0.0005 = 100.00€
        Integer ageAdhesion = 45;
        Integer durationYears = 20;
        Double loanAmount = 200000.0;

        // Act
        Map<String, Double> premiums = warrantyCalculationService.calculerPrimesPuresDesGaranties(
                warranty,
                configGaranties,
                ageAdhesion,
                durationYears,
                loanAmount,
                RESOLVED_QUOTITY_FACTOR);

        // Assert
        double premium = premiums.get("IP");
        assertThat(premium).isCloseTo(100.0, within(0.01));
        // Vérifie qu'il n'y a pas plus de 2 décimales (comparaison numérique)
        assertThat(premium * 100).isCloseTo(10000.0, within(0.01));
    }

    // ==================== Helpers ====================

    /**
     * Construit une liste de configurations de garanties pour les tests.
     * Configuration: IP, IPP (dÃ©pend IP), ITT
     */
    private List<ConfigGarantie> buildSampleConfigGaranties() {
        List<ConfigGarantie> result = new ArrayList<>();

        // IP configuration
        List<PrimePureCi> ipCoefficients = List.of(
                new PrimePureCi(45, 20, 0.0005)
        );

        ConfigGarantie ip = new ConfigGarantie(
                new Reference("IP", "InvaliditÃ© Permanente"),
                false,
                18,
                65,
                null,
                null,
                null,
                null,
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                ipCoefficients,
                Collections.emptyList()
        );
        result.add(ip);

        // IPP configuration (dÃ©pend de IP)
        List<PrimePureCi> ippCoefficients = List.of(
                new PrimePureCi(45, 20, 0.0003)
        );

        List<Dependance> ippDependencies = List.of(
                new Dependance(new Reference("IP", "InvaliditÃ© Permanente"))
        );

        ConfigGarantie ipp = new ConfigGarantie(
                new Reference("IPP", "InvaliditÃ© Permanente Partielle"),
                false,
                18,
                65,
                null,
                null,
                null,
                null,
                ippDependencies,
                Collections.emptyList(),
                Collections.emptyList(),
                ippCoefficients,
                Collections.emptyList()
        );
        result.add(ipp);

        // ITT configuration
        List<PrimePureCi> ittCoefficients = List.of(
                new PrimePureCi(45, 20, 0.00085)
        );

        ConfigGarantie itt = new ConfigGarantie(
                new Reference("ITT", "IncapacitÃ© Temporaire Totale"),
                false,
                18,
                65,
                null,
                null,
                null,
                null,
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                ittCoefficients,
                Collections.emptyList()
        );
        result.add(itt);

        return result;
    }

    // ==================== Nouveaux tests: chaîne de coefficients ====================

    private WarrantyPricingContext contextWith(DetailConfig detailConfig,
                                               Boolean couple, Boolean smoker, Boolean lemoineProfile,
                                               String typePret, String objetPret, String csp,
                                               Double totalCapital,
                                               List<TypePretCoefficient> typePretCoefs,
                                               List<ObjetPretCoefficient> objetPretCoefs,
                                               List<ClasseRisqueCoefficient> classeRisqueCoefs,
                                               List<CoefficientPassageFumeurCi> fumeurCoefs,
                                               List<ProfessionClasseRisqueMapping> professionMappings) {
        return contextWith(detailConfig, couple, smoker, lemoineProfile,
            typePret, objetPret, csp, totalCapital,
            typePretCoefs, objetPretCoefs, classeRisqueCoefs, fumeurCoefs,
            professionMappings, null);
    }

    private WarrantyPricingContext contextWith(DetailConfig detailConfig,
                                               Boolean couple, Boolean smoker, Boolean lemoineProfile,
                                               String typePret, String objetPret, String csp,
                                               Double totalCapital,
                                               List<TypePretCoefficient> typePretCoefs,
                                               List<ObjetPretCoefficient> objetPretCoefs,
                                               List<ClasseRisqueCoefficient> classeRisqueCoefs,
                                               List<CoefficientPassageFumeurCi> fumeurCoefs,
                                               List<ProfessionClasseRisqueMapping> professionMappings,
                                               String franchiseCode) {
        return new WarrantyPricingContext(
            detailConfig, couple, smoker, lemoineProfile,
            typePret, objetPret, csp, totalCapital,
            typePretCoefs, objetPretCoefs, classeRisqueCoefs, fumeurCoefs,
            null, professionMappings, franchiseCode
        );
    }

    @Test
    void shouldApplyGrosCapitalCoefficientOnlyWhenThresholdExceededAtLoanLevel() {
        // Arrange
        Warranty warranty = new Warranty(true, false, false, false, false, false, false, false, "100%", "100%");
        DetailConfig detail = new DetailConfig(null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null,
            300000.0, 110.0, null);

        WarrantyPricingContext ctxBelow = contextWith(detail, null, null, null, null, null, null,
            200000.0, null, null, null, null, null);
        WarrantyPricingContext ctxAbove = contextWith(detail, null, null, null, null, null, null,
            350000.0, null, null, null, null, null);

        // Act
        double below = warrantyCalculationService.calculerPrimesPuresDesGaranties(
            warranty, configGaranties, 45, 20, 200000.0, 1.0, ctxBelow).get("IP");
        double above = warrantyCalculationService.calculerPrimesPuresDesGaranties(
            warranty, configGaranties, 45, 20, 200000.0, 1.0, ctxAbove).get("IP");

        // Assert : below = base 1.00€, above = 1.00 × 1.10 = 1.10€
        assertThat(below).isCloseTo(100.0, within(0.01));
        assertThat(above).isCloseTo(110.0, within(0.01));
    }

    @Test
    void shouldApplyCoupleAndExonerationCoefficients() {
        // Arrange : couple = 90 (×0.9), exoneration = 95 (×0.95), option souscrite dans le contexte
        Warranty warranty = new Warranty(true, false, false, false, false, false, false, false, "100%", "100%");
        DetailConfig detail = new DetailConfig(null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, 95.0, null, null, 90.0);

        WarrantyPricingContext ctx = new WarrantyPricingContext(
            detail, true, null, null, null, null, null, 100000.0,
            null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, true);

        // Act
        double premium = warrantyCalculationService.calculerPrimesPuresDesGaranties(
            warranty, configGaranties, 45, 20, 200000.0, 1.0, ctx).get("IP");

        // Assert : 1.00 × 0.90 × 0.95 = 0.855 → arrondi 0.86
        assertThat(premium).isCloseTo(85.5, within(0.01));
    }

    @Test
    void shouldApplyTypePretAndObjetPretCoefficients() {
        // Arrange
        Warranty warranty = new Warranty(true, false, false, false, false, false, false, false, "100%", "100%");
        List<TypePretCoefficient> typeCoefs = List.of(
            new TypePretCoefficient(new Reference("PRET_AMORTISSABLE", "Prêt amortissable"), "VIE", null, 120.0)
        );
        List<ObjetPretCoefficient> objetCoefs = List.of(
            new ObjetPretCoefficient(new Reference("RESIDENCE_PRINCIPALE", "Résidence Principale"), null, 80.0)
        );

        WarrantyPricingContext ctx = contextWith(null, null, null, null,
            "PRET_AMORTISSABLE", "RESIDENCE_PRINCIPALE", null, null,
            typeCoefs, objetCoefs, null, null, null);

        // Act
        double premium = warrantyCalculationService.calculerPrimesPuresDesGaranties(
            warranty, configGaranties, 45, 20, 200000.0, 1.0, ctx).get("IP");

        // Assert : 1.00 × 1.20 × 0.80 = 0.96
        assertThat(premium).isCloseTo(96.0, within(0.01));
    }

    @Test
    void shouldApplyFumeurCoefficientWhenSmoker() {
        // Arrange
        Warranty warranty = new Warranty(true, false, false, false, false, false, false, false, "100%", "100%");
        List<CoefficientPassageFumeurCi> fumeurCoefs = List.of(
            new CoefficientPassageFumeurCi(45, "NON_VIE", 150.0)
        );

        WarrantyPricingContext ctxSmoker = contextWith(null, null, true, null, null, null, null,
            null, null, null, null, fumeurCoefs, null);
        WarrantyPricingContext ctxNonSmoker = contextWith(null, null, false, null, null, null, null,
            null, null, null, null, fumeurCoefs, null);

        // Act
        double smoker = warrantyCalculationService.calculerPrimesPuresDesGaranties(
            warranty, configGaranties, 45, 20, 200000.0, 1.0, ctxSmoker).get("IP");
        double nonSmoker = warrantyCalculationService.calculerPrimesPuresDesGaranties(
            warranty, configGaranties, 45, 20, 200000.0, 1.0, ctxNonSmoker).get("IP");

        // Assert : fumeur = 1.00 × 1.50 = 1.50 ; non-fumeur = 1.00 (coef ignoré)
        assertThat(smoker).isCloseTo(150.0, within(0.01));
        assertThat(nonSmoker).isCloseTo(100.0, within(0.01));
    }

    @Test
    void shouldApplyLemoineCoefficientAbove31() {
        // Arrange
        Warranty warranty = new Warranty(true, false, false, false, false, false, false, false, "100%", "100%");
        List<PrimePureCi> ipCoefs = List.of(new PrimePureCi(35, 20, 0.0005));
        ConfigGarantie ip = new ConfigGarantie(
            new Reference("IP", "Invalidité"), false, 18, 65, null, null, null, null,
            Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
            ipCoefs, Collections.emptyList()
        );
        List<ConfigGarantie> configs = List.of(ip);

        List<CoefficientPerimetreLemoineCi> lemoineCoefs = List.of(
            new CoefficientPerimetreLemoineCi(35, "NON_VIE", 109.5)
        );
        WarrantyPricingContext base = contextWith(null, null, null, true, null, null, null,
            null, null, null, null, null, null);
        WarrantyPricingContext ctxLemoine = new WarrantyPricingContext(
            base.detailConfig(), base.couple(), base.smoker(), base.lemoineProfile(),
            base.typePret(), base.objetPret(), base.cspCode(), base.loanInsuredCapitalNonVie(),
            base.typePretCoefficients(), base.objetPretCoefficients(), base.classeRisqueCoefficients(),
            base.coefficientsPassageFumeurCi(), base.mappingsCategoriePro(), base.mappingsProfession(),
            base.franchiseCode(), base.mnoOption(), null, base.drom(), base.corse(), base.iptSortieCapital(),
            base.ageFinCouvertureOption(), base.coverageEndCoefficients(), base.territorialiteCoefficientsNonVie(),
            base.exonerationCotisations(), lemoineCoefs);

        // Act : âge 35 → coefficient Lemoine 109.5 (×1.095)
        double premium = warrantyCalculationService.calculerPrimesPuresDesGaranties(
            warranty, configs, 35, 20, 200000.0, 1.0, ctxLemoine).get("IP");

        // Assert : 1.00 × 1.095 = 1.095 → arrondi 1.10
        assertThat(premium).isCloseTo(109.5, within(0.01));
    }

    @Test
    void shouldApplyCspCoefficientViaProfessionMapping() {
        // Arrange
        Warranty warranty = new Warranty(true, false, false, false, false, false, false, false, "100%", "100%");
        List<ProfessionClasseRisqueMapping> professionMappings = List.of(
            new ProfessionClasseRisqueMapping(new Reference("2", "Développeur"), "NON_VIE",
                new Reference("CR1", "Classe risque 1"))
        );
        List<ClasseRisqueCoefficient> classeCoefs = List.of(
            new ClasseRisqueCoefficient(new Reference("CR1", "Classe risque 1"), "NON_VIE", null, 130.0)
        );

        WarrantyPricingContext ctx = contextWith(null, null, null, null, null, null, "2",
            null, null, null, classeCoefs, null, professionMappings);

        // Act
        double premium = warrantyCalculationService.calculerPrimesPuresDesGaranties(
            warranty, configGaranties, 45, 20, 200000.0, 1.0, ctx).get("IP");

        // Assert : 1.00 × 1.30 = 1.30
        assertThat(premium).isCloseTo(130.0, within(0.01));
    }

    @Test
    void shouldIgnoreCoefficientsWhenContextIsEmpty() {
        // Arrange
        Warranty warranty = new Warranty(true, false, false, false, false, false, false, false, "100%", "100%");

        // Act : contexte vide → aucun multiplicateur appliqué
        double premium = warrantyCalculationService.calculerPrimesPuresDesGaranties(
            warranty, configGaranties, 45, 20, 200000.0, 1.0, WarrantyPricingContext.empty()).get("IP");

        // Assert : prime de base 1.00€
        assertThat(premium).isCloseTo(100.0, within(0.01));
    }

    @Test
    void shouldApplyFullPricingChainInExpectedOrder() {
        // Arrange : csp=110, typePret=120, objetPret=90, fumeur=150,
        //          couple=95, grosCapital=105, exoneration=98, lemoine=109.5
        Warranty warranty = new Warranty(true, false, false, false, false, false, false, false, "100%", "100%");
        DetailConfig detail = new DetailConfig(null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null,
            98.0, 150000.0, 105.0, 95.0);
        List<TypePretCoefficient> typeCoefs = List.of(
            new TypePretCoefficient(new Reference("PRET_AMORTISSABLE", "Prêt"), "VIE", null, 120.0)
        );
        List<ObjetPretCoefficient> objetCoefs = List.of(
            new ObjetPretCoefficient(new Reference("RESIDENCE_PRINCIPALE", "RP"), null, 90.0)
        );
        List<CoefficientPassageFumeurCi> fumeurCoefs = List.of(
            new CoefficientPassageFumeurCi(45, "NON_VIE", 150.0)
        );
        List<ProfessionClasseRisqueMapping> professionMappings = List.of(
            new ProfessionClasseRisqueMapping(new Reference("2", "Dev"), "NON_VIE",
                new Reference("CR1", "Classe 1"))
        );
        List<ClasseRisqueCoefficient> classeCoefs = List.of(
            new ClasseRisqueCoefficient(new Reference("CR1", "Classe 1"), "NON_VIE", null, 110.0)
        );

        WarrantyPricingContext base = contextWith(detail, true, true, true,
            "PRET_AMORTISSABLE", "RESIDENCE_PRINCIPALE", "2",
            200000.0,
            typeCoefs, objetCoefs, classeCoefs, fumeurCoefs, professionMappings);
        WarrantyPricingContext ctx = new WarrantyPricingContext(
            base.detailConfig(), base.couple(), base.smoker(), base.lemoineProfile(),
            base.typePret(), base.objetPret(), base.cspCode(), base.loanInsuredCapitalNonVie(),
            base.typePretCoefficients(), base.objetPretCoefficients(), base.classeRisqueCoefficients(),
            base.coefficientsPassageFumeurCi(), base.mappingsCategoriePro(), base.mappingsProfession(),
            base.franchiseCode(), base.mnoOption(), null, base.drom(), base.corse(), base.iptSortieCapital(),
            base.ageFinCouvertureOption(), base.coverageEndCoefficients(), base.territorialiteCoefficientsNonVie(),
            true, List.of(new CoefficientPerimetreLemoineCi(45, "NON_VIE", 109.5)));

        // Act
        double premium = warrantyCalculationService.calculerPrimesPuresDesGaranties(
            warranty, configGaranties, 45, 20, 200000.0, 1.0, ctx).get("IP");

        // Assert : 1.00 × 1.10 × 1.20 × 0.90 × 1.50 × 0.95 × 1.05 × 0.98 × 1.095
        double expected = 100.0 * 1.10 * 1.20 * 0.90 * 1.50 * 0.95 * 1.05 * 0.98 * 1.095;
        assertThat(premium).isCloseTo(expected, within(0.02));
    }

    @Test
    void shouldApplyFranchiseCoefficientForNonVieWhenLemoineMatches() {
        // Arrange : IP (NON_VIE) avec franchise FR_90J, coefficient 85 pour Lemoine=false
        Warranty warranty = new Warranty(true, false, false, false, false, false, false, false, "100%", "100%");
        List<PrimePureCi> ipCoefs = List.of(new PrimePureCi(45, 20, 0.0005));
        List<FranchiseCoefficient> franchises = List.of(
            new FranchiseCoefficient(new Reference("FR_90J", "Franchise 90j"), true, 105.0),
            new FranchiseCoefficient(new Reference("FR_90J", "Franchise 90j"), false, 85.0)
        );
        ConfigGarantie ip = new ConfigGarantie(
            new Reference("IP", "Invalidité"), false, 18, 65, null, null, null, null,
            Collections.emptyList(), franchises, Collections.emptyList(),
            ipCoefs, Collections.emptyList()
        );
        List<ConfigGarantie> configs = List.of(ip);

        // Client Lemoine=false (disclosedOverLemoineLimit=true) → cherche coefficient 85
        WarrantyPricingContext ctx = contextWith(null, null, null, false, null, null, null,
            null, null, null, null, null, null, "FR_90J");

        // Act
        double premium = warrantyCalculationService.calculerPrimesPuresDesGaranties(
            warranty, configs, 45, 20, 200000.0, 1.0, ctx).get("IP");

        // Assert : base 1.00 × 0.85 = 0.85
        assertThat(premium).isCloseTo(85.0, within(0.01));
    }

    @Test
    void shouldIgnoreFranchiseCoefficientWhenLemoineDoesNotMatch() {
        // Arrange : franchise FR_90J uniquement pour regimeLemoine=true, client Lemoine=false
        Warranty warranty = new Warranty(true, false, false, false, false, false, false, false, "100%", "100%");
        List<PrimePureCi> ipCoefs = List.of(new PrimePureCi(45, 20, 0.0005));
        List<FranchiseCoefficient> franchises = List.of(
            new FranchiseCoefficient(new Reference("FR_90J", "Franchise 90j"), true, 105.0)
        );
        ConfigGarantie ip = new ConfigGarantie(
            new Reference("IP", "Invalidité"), false, 18, 65, null, null, null, null,
            Collections.emptyList(), franchises, Collections.emptyList(),
            ipCoefs, Collections.emptyList()
        );
        List<ConfigGarantie> configs = List.of(ip);

        WarrantyPricingContext ctx = contextWith(null, null, null, false, null, null, null,
            null, null, null, null, null, null, "FR_90J");

        // Act
        double premium = warrantyCalculationService.calculerPrimesPuresDesGaranties(
            warranty, configs, 45, 20, 200000.0, 1.0, ctx).get("IP");

        // Assert : coefficient ignoré (×1.0) → prime de base 1.00
        assertThat(premium).isCloseTo(100.0, within(0.01));
    }

    @Test
    void shouldIgnoreFranchiseWhenFranchiseCodeIsNull() {
        // Arrange : config avec franchises mais aucun franchiseCode dans le contexte → coefficient ignoré
        Warranty warranty = new Warranty(true, false, false, false, false, false, false, false, "100%", "100%");
        List<PrimePureCi> ipCoefs = List.of(new PrimePureCi(45, 20, 0.0005));
        List<FranchiseCoefficient> franchises = List.of(
            new FranchiseCoefficient(new Reference("FR_90J", "Franchise 90j"), false, 85.0)
        );
        ConfigGarantie ip = new ConfigGarantie(
            new Reference("IP", "Invalidité"), false, 18, 65, null, null, null, null,
            Collections.emptyList(), franchises, Collections.emptyList(),
            ipCoefs, Collections.emptyList()
        );
        List<ConfigGarantie> configs = List.of(ip);

        WarrantyPricingContext ctx = contextWith(null, null, null, false, null, null, null,
            null, null, null, null, null, null, null);

        // Act
        double premium = warrantyCalculationService.calculerPrimesPuresDesGaranties(
            warranty, configs, 45, 20, 200000.0, 1.0, ctx).get("IP");

        // Assert : franchiseCode null → coefficient ignoré → prime de base 1.00
        assertThat(premium).isCloseTo(100.0, within(0.01));
    }

    @Test
    void shouldCalculateIppUsingIttAndIptBasePremiums() {
        ConfigGarantie ipt = new ConfigGarantie(
            new Reference("IPT", "Invalidité Permanente Totale"),
            false, 18, 65, null, null, null, null,
            Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
            List.of(new PrimePureCi(45, 20, 0.0005)), Collections.emptyList()
        );
        ConfigGarantie itt = new ConfigGarantie(
            new Reference("ITT", "Incapacité Temporaire Totale"),
            false, 18, 65, null, null, null, null,
            Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
            List.of(new PrimePureCi(45, 20, 0.0005)), Collections.emptyList()
        );
        ConfigGarantie ipp = new ConfigGarantie(
            new Reference("IPP", "Invalidité Permanente Partielle"),
            false, 18, 65, null, null, null, null,
            Collections.emptyList(), Collections.emptyList(),
            List.of(new OptionCoefficient(new Reference("IPP_VAR_50", "Variante 50 %"), false, 125.0)),
            Collections.emptyList(), Collections.emptyList()
        );
        Warranty warranty = new Warranty(
            false, true, true, false, true, false, false, false,
            "100%", "100%"
        );

        Map<String, Double> premiums = warrantyCalculationService.calculerPrimesPuresDesGaranties(
            warranty, List.of(ipt, itt, ipp), 45, 20, 200000.0, 1.0, WarrantyPricingContext.empty());

        assertThat(premiums.get("IPP")).isCloseTo(50.0, within(0.01));
    }

    @Test
    void shouldCalculateIppPremiumFromOptionCoefficient() {
        ConfigGarantie ipt = new ConfigGarantie(
            new Reference("IPT", "Invalidité Permanente Totale"),
            false, 18, 65, null, null, null, null,
            Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
            List.of(new PrimePureCi(45, 20, 0.0005)), Collections.emptyList()
        );
        ConfigGarantie itt = new ConfigGarantie(
            new Reference("ITT", "Incapacité Temporaire Totale"),
            false, 18, 65, null, null, null, null,
            Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
            List.of(new PrimePureCi(45, 20, 0.0005)), Collections.emptyList()
        );
        ConfigGarantie ipp = new ConfigGarantie(
            new Reference("IPP", "Invalidité Permanente Partielle"),
            false, 18, 65, null, null, null, null,
            Collections.emptyList(), Collections.emptyList(),
            List.of(new OptionCoefficient(new Reference("IPP_VAR_50", "Variante 50 %"), false, 228.0)),
            Collections.emptyList(), Collections.emptyList()
        );
        Warranty warranty = new Warranty(
            false, true, true, false, true, false, false, false,
            "100%", "100%"
        );

        Map<String, Double> premiums = warrantyCalculationService.calculerPrimesPuresDesGaranties(
            warranty, List.of(ipt, itt, ipp), 45, 20, 200000.0, 1.0, WarrantyPricingContext.empty());

        assertThat(premiums.get("IPP")).isCloseTo(256.0, within(0.01));
    }

}
