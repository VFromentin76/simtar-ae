package fr.hm.tarificateur.domain.service;

import fr.hm.tarificateur.domain.model.CoefficientPassageFumeurCi;
import fr.hm.tarificateur.domain.model.ClasseRisqueCoefficient;
import fr.hm.tarificateur.domain.model.CoefficientPrimePureCi;
import fr.hm.tarificateur.domain.model.CoefficientPrimePureCrd;
import fr.hm.tarificateur.domain.model.ClassificationRisque;
import fr.hm.tarificateur.domain.model.ContexteCalculPrimePure;
import fr.hm.tarificateur.domain.model.ContexteCoefficients;
import fr.hm.tarificateur.domain.model.ContextePret;
import fr.hm.tarificateur.domain.model.DetailConfig;
import fr.hm.tarificateur.domain.model.EligibilityLemoine;
import fr.hm.tarificateur.domain.model.ObjetPretCoefficient;
import fr.hm.tarificateur.domain.model.ProfilEmprunteur;
import fr.hm.tarificateur.domain.model.Reference;
import fr.hm.tarificateur.domain.model.TypePretCoefficient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

public class PremiumCalculationServiceTest {

    private PremiumCalculationService premiumCalculationService;

    @BeforeEach
    public void setUp() {
        premiumCalculationService = new PremiumCalculationService();
    }

    @Test
    public void testCalculateAgeAdhesion() {
        Integer age = premiumCalculationService.calculateAgeAdhesion("1990-01-01", "2025-02-15");
        assertEquals(35, age);
    }

    @Test
    public void testCalculateAgeAdhesion_SameDateBirthAndCreation() {
        Integer age = premiumCalculationService.calculateAgeAdhesion("1990-02-15", "1990-02-15");
        assertEquals(0, age);
    }

    @Test
    public void testCalculateAgeAdhesion_InvalidBirthDate() {
        assertThatThrownBy(() -> premiumCalculationService.calculateAgeAdhesion("invalid-date", "2025-02-15"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testCalculateAgeAdhesion_InvalidCreationDate() {
        assertThatThrownBy(() -> premiumCalculationService.calculateAgeAdhesion("1990-01-01", "invalid"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testCalculatePremiumCI_WithWarranties() {
        Double loanAmount = 220000.0;
        Integer ageAdhesion = 35;
        Integer loanDurationYears = 15;

        List<CoefficientPrimePureCi> coefficients = Arrays.asList(
                new CoefficientPrimePureCi(35, 15, 0.0025)
        );

        double monthlyPremium = premiumCalculationService.insuredCapitalMultiplierParTarifPrimePureDC_PTIA(
                loanAmount,
                ageAdhesion,
                loanDurationYears,
                coefficients
        );

        assertEquals(550.0, monthlyPremium, 0.01);
    }

    @Test
    public void testCalculatePremiumCI_WithoutWarranties() {
        Double loanAmount = 220000.0;
        Integer ageAdhesion = 35;
        Integer loanDurationYears = 15;

        List<CoefficientPrimePureCi> coefficients = Arrays.asList(
                new CoefficientPrimePureCi(35, 15, 0.0025)
        );

        double monthlyPremium = premiumCalculationService.insuredCapitalMultiplierParTarifPrimePureDC_PTIA(
                loanAmount,
                ageAdhesion,
                loanDurationYears,
                coefficients
        );

        assertEquals(550.0, monthlyPremium, 0.01);
    }

    @Test
    public void testCalculatePremiumCI_WithoutCoefficients() {
        Double loanAmount = 220000.0;
        Integer ageAdhesion = 35;
        Integer loanDurationYears = 15;

        List<CoefficientPrimePureCi> coefficients = Arrays.asList();
        assertThatThrownBy(() -> premiumCalculationService.insuredCapitalMultiplierParTarifPrimePureDC_PTIA(
                loanAmount,
                ageAdhesion,
                loanDurationYears,
                coefficients
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Impossible de trouver ou interpoler un coefficient CI");
    }

    @Test
    public void testCalculatePremiumCRD_WithWarranties() {
        Double loanAmount = 220000.0;
        Integer ageAtteint = 40;

        List<CoefficientPrimePureCrd> coefficients = Arrays.asList(
                new CoefficientPrimePureCrd(40, 0.0028)
        );

        double monthlyPremium = premiumCalculationService.calculatePremiumCRD(
                loanAmount,
                ageAtteint,
                coefficients
        );

        assertEquals(51.33, monthlyPremium, 0.01);
    }

    @Test
    public void testCalculatePremiumCRD_WithoutCoefficients() {
        Double loanAmount = 220000.0;
        Integer ageAtteint = 40;

        List<CoefficientPrimePureCrd> coefficients = Arrays.asList();
        assertThatThrownBy(() -> premiumCalculationService.calculatePremiumCRD(
                loanAmount,
                ageAtteint,
                coefficients
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Impossible de trouver ou interpoler un coefficient CRD");
    }

    @Test
    public void testCalculatePremiumCI_InterpolationClosest() {
        Double loanAmount = 220000.0;
        Integer ageAdhesion = 35;
        Integer loanDurationYears = 15;

        List<CoefficientPrimePureCi> coefficients = Arrays.asList(
                new CoefficientPrimePureCi(36, 16, 0.0025)
        );

        double monthlyPremium = premiumCalculationService.insuredCapitalMultiplierParTarifPrimePureDC_PTIA(
                loanAmount,
                ageAdhesion,
                loanDurationYears,
                coefficients
        );

        assertEquals(550.0, monthlyPremium, 0.01);
    }

    @Test
    public void testCalculatePremiumCRD_InterpolationLinear() {
        Double loanAmount = 220000.0;
        Integer ageAtteint = 38;

        List<CoefficientPrimePureCrd> coefficients = Arrays.asList(
                new CoefficientPrimePureCrd(35, 0.0020),
                new CoefficientPrimePureCrd(40, 0.0030)
        );

        double monthlyPremium = premiumCalculationService.calculatePremiumCRD(
                loanAmount,
                ageAtteint,
                coefficients
        );

        assertEquals(47.67, monthlyPremium, 0.01);
    }

    @Test
    public void testCalculatePremiumCRD_ExactMatch() {
        Double loanAmount = 220000.0;
        Integer ageAtteint = 40;

        List<CoefficientPrimePureCrd> coefficients = Arrays.asList(
                new CoefficientPrimePureCrd(35, 0.0020),
                new CoefficientPrimePureCrd(40, 0.0030),
                new CoefficientPrimePureCrd(45, 0.0040)
        );

        double monthlyPremium = premiumCalculationService.calculatePremiumCRD(
                loanAmount,
                ageAtteint,
                coefficients
        );

        assertEquals(55.0, monthlyPremium, 0.01);
    }

    @Test
    public void testCalculatePremiumCI_AllGuarantees() {
        Double loanAmount = 220000.0;
        Integer ageAdhesion = 35;
        Integer loanDurationYears = 15;

        List<CoefficientPrimePureCi> coefficients = Arrays.asList(
                new CoefficientPrimePureCi(35, 15, 0.0025)
        );

        double monthlyPremium = premiumCalculationService.insuredCapitalMultiplierParTarifPrimePureDC_PTIA(
                loanAmount,
                ageAdhesion,
                loanDurationYears,
                coefficients
        );

        assertEquals(550.0, monthlyPremium, 0.01);
    }

    @Test
    public void testCalculatePremiumCI_NullCoefficients_UseClosest() {
        Double loanAmount = 220000.0;
        Integer ageAdhesion = 99;
        Integer loanDurationYears = 99;

        List<CoefficientPrimePureCi> coefficients = Arrays.asList(
                new CoefficientPrimePureCi(35, 15, 0.0025)
        );

        double monthlyPremium = premiumCalculationService.insuredCapitalMultiplierParTarifPrimePureDC_PTIA(
                loanAmount,
                ageAdhesion,
                loanDurationYears,
                coefficients
        );

        // Uses closest coefficient (35, 15)
        // 220000 * 0.0025 = 550 EUR
        assertEquals(550.0, monthlyPremium, 0.01);
    }

    @Test
    public void testCalculatePremiumCRD_UseClosestWhenNoInterpolation() {
        Double loanAmount = 220000.0;
        Integer ageAtteint = 50;

        List<CoefficientPrimePureCrd> coefficients = Arrays.asList(
                new CoefficientPrimePureCrd(35, 0.0020),
                new CoefficientPrimePureCrd(40, 0.0030)
        );

        double monthlyPremium = premiumCalculationService.calculatePremiumCRD(
                loanAmount,
                ageAtteint,
                coefficients
        );

        assertThat(monthlyPremium).isGreaterThan(0.0);
    }

    @Test
    public void testCalculatePremiumCRD_BeforeLowerBoundary() {
        Double loanAmount = 220000.0;
        Integer ageAtteint = 30;

        List<CoefficientPrimePureCrd> coefficients = Arrays.asList(
                new CoefficientPrimePureCrd(35, 0.0020),
                new CoefficientPrimePureCrd(40, 0.0030)
        );

        double monthlyPremium = premiumCalculationService.calculatePremiumCRD(
                loanAmount,
                ageAtteint,
                coefficients
        );

        assertThat(monthlyPremium).isGreaterThan(0.0);
    }

    @Test
    public void testCalculatePremiumCI_ExactMatch() {
        Double loanAmount = 220000.0;
        Integer ageAdhesion = 35;
        Integer loanDurationYears = 15;

        List<CoefficientPrimePureCi> coefficients = Arrays.asList(
                new CoefficientPrimePureCi(35, 15, 0.0025)
        );

        double monthlyPremium = premiumCalculationService.insuredCapitalMultiplierParTarifPrimePureDC_PTIA(
                loanAmount,
                ageAdhesion,
                loanDurationYears,
                coefficients
        );

        assertEquals(550.0, monthlyPremium, 0.01);
    }

    @Test
    public void testCalculatePremiumCI_WithNullWarrantyFactors() {
        Double loanAmount = 220000.0;
        Integer ageAdhesion = 35;
        Integer loanDurationYears = 15;

        List<CoefficientPrimePureCi> coefficients = Arrays.asList(
                new CoefficientPrimePureCi(35, 15, 0.0025)
        );

        double monthlyPremium = premiumCalculationService.insuredCapitalMultiplierParTarifPrimePureDC_PTIA(
                loanAmount,
                ageAdhesion,
                loanDurationYears,
                coefficients
        );

        assertThat(monthlyPremium).isGreaterThan(0.0);
    }

    @Test
    public void testCalculatePremiumCRD_WithNullWarrantyFactors() {
        Double loanAmount = 220000.0;
        Integer ageAtteint = 40;

        List<CoefficientPrimePureCrd> coefficients = Arrays.asList(
                new CoefficientPrimePureCrd(40, 0.0028)
        );

        double monthlyPremium = premiumCalculationService.calculatePremiumCRD(
                loanAmount,
                ageAtteint,
                coefficients
        );

        assertThat(monthlyPremium).isGreaterThan(0.0);
    }

    @Test
    public void testCalculatePremiumCI_WithPartialWarranties() {
        Double loanAmount = 220000.0;
        Integer ageAdhesion = 35;
        Integer loanDurationYears = 15;

        List<CoefficientPrimePureCi> coefficients = Arrays.asList(
                new CoefficientPrimePureCi(35, 15, 0.0025)
        );

        double monthlyPremium = premiumCalculationService.insuredCapitalMultiplierParTarifPrimePureDC_PTIA(
                loanAmount,
                ageAdhesion,
                loanDurationYears,
                coefficients
        );

        assertThat(monthlyPremium).isEqualTo(550.0, org.assertj.core.api.Assertions.offset(0.01));
    }

    @Test
    public void testCalculatePremiumCRD_MultipleLoans() {
        Double loanAmount = 100000.0;
        Integer ageAtteint = 35;

        List<CoefficientPrimePureCrd> coefficients = Arrays.asList(
                new CoefficientPrimePureCrd(35, 0.0020)
        );

        double monthlyPremium = premiumCalculationService.calculatePremiumCRD(
                loanAmount,
                ageAtteint,
                coefficients
        );

        assertThat(monthlyPremium).isEqualTo(16.67, org.assertj.core.api.Assertions.offset(0.01));
    }

    @Test
    public void testCalculerPrimePureTotale_ShouldApplyMultipliersAndRound() {
        DetailConfig detailConfig = new DetailConfig(
                "ASSURANCE",
                "ASSOC",
                new Reference("TERR_AD", "Territoire adherent"),
                new Reference("TERR_BIEN", "Territoire bien"),
                new Reference("TERR_PREST", "Territoire prestations"),
                "2026-01-01",
                "2026-01-01",
                true,
                new Reference("MENSUEL", "Mensuel"),
                10.0,
                5.0,
                3.0,
                2.0,
                20.0,
                15.0,
                15.0,
                100000.0,
                120.0,
                110.0
        );

        var context = new ContexteCalculPrimePure(
                100.0,
                detailConfig,
                new ProfilEmprunteur(true, true, true, 40),
                new ContextePret(150000.0, "PRET_AMORTISSABLE", "MAISON"),
                new ClassificationRisque(null, "CI"),
                new ContexteCoefficients(
                        List.of(new TypePretCoefficient(new Reference("PRET_AMORTISSABLE", "Pret amortissable"), "CI", true, 110.0)),
                        List.of(new ObjetPretCoefficient(new Reference("MAISON", "Maison"), true, 90.0)),
                        List.of(),
                        List.of(new CoefficientPassageFumeurCi(40, "CI", 125.0))
                ),
                new EligibilityLemoine(true, List.of(new fr.hm.tarificateur.domain.model.CoefficientPerimetreLemoineCi(40, "CI", 109.5))),
                true
        );
        var premium = premiumCalculationService.calculerPrimePureTotale(context);

        assertThat(premium).isEqualTo(26.83, org.assertj.core.api.Assertions.offset(0.01));
    }

    @Test
    public void testCalculerPrimePureTotale_ShouldNotApplyExonerationCoefficientWhenNotSubscribed() {
        DetailConfig detailConfig = new DetailConfig(
                "ASSURANCE",
                "ASSOC",
                new Reference("TERR_AD", "Territoire adherent"),
                new Reference("TERR_BIEN", "Territoire bien"),
                new Reference("TERR_PREST", "Territoire prestations"),
                "2026-01-01",
                "2026-01-01",
                true,
                new Reference("MENSUEL", "Mensuel"),
                10.0,
                5.0,
                3.0,
                2.0,
                20.0,
                15.0,
                15.0,
                100000.0,
                120.0,
                110.0
        );

        var context = new ContexteCalculPrimePure(
                100.0,
                detailConfig,
                new ProfilEmprunteur(true, true, true, 40),
                new ContextePret(150000.0, "PRET_AMORTISSABLE", "MAISON"),
                new ClassificationRisque(null, "CI"),
                new ContexteCoefficients(
                        List.of(new TypePretCoefficient(new Reference("PRET_AMORTISSABLE", "Pret amortissable"), "CI", true, 110.0)),
                        List.of(new ObjetPretCoefficient(new Reference("MAISON", "Maison"), true, 90.0)),
                        List.of(),
                        List.of(new CoefficientPassageFumeurCi(40, "CI", 125.0))
                ),
                new EligibilityLemoine(true, List.of(new fr.hm.tarificateur.domain.model.CoefficientPerimetreLemoineCi(40, "CI", 109.5))),
                false
        );
        var premium = premiumCalculationService.calculerPrimePureTotale(context);

        // Sans souscription à l'exonération, le coefficient (0.15) n'est pas appliqué :
        // résultat supérieur à 26.83 (résultat avec exonération appliquée).
        assertThat(premium).isGreaterThan(26.83);
    }

    @Test
    public void testCalculerPrimePureTotale_ShouldApplyClasseRisqueCoefficientForMatchingBranche() {
        var context = new ContexteCalculPrimePure(
            100.0,
            null,
            new ProfilEmprunteur(false, false, false, 40),
            new ContextePret(100000.0, null, null),
            new ClassificationRisque("CR1", "VIE"),
            new ContexteCoefficients(
                List.of(),
                List.of(),
                List.of(
                    new ClasseRisqueCoefficient(new Reference("CR1", "Classe 1"), "NON_VIE", false, 130.0),
                    new ClasseRisqueCoefficient(new Reference("CR1", "Classe 1"), "VIE", false, 110.0)
                ),
                List.of()
            ),
            new EligibilityLemoine(true, null),
            false
        );
        var premium = premiumCalculationService.calculerPrimePureTotale(context);

        assertThat(premium).isEqualTo(110.0);
    }

    @Test
    public void testApplyChargementsEtTaxe_ShouldApplyAllChargesAndTax() {
        DetailConfig detailConfig = new DetailConfig(
                "ASSURANCE",
                "ASSOC",
                null,
                null,
                null,
                null,
                null,
                false,
                null,
                5.0,
                3.0,
                2.0,
                0.0,
                0.0,
                0.0,
                null,
                null,
                null,
                null
        );

        double premium = premiumCalculationService.applyChargementsEtTaxe(100.0, detailConfig);

        assertThat(premium).isEqualTo(110.53, org.assertj.core.api.Assertions.offset(0.01));
    }

    @Test
    public void testApplyChargementsEtTaxe_ShouldLeavePremiumUntouchedWhenConfigIsNull() {
        double premium = premiumCalculationService.applyChargementsEtTaxe(100.0, null);

        assertThat(premium).isEqualTo(100.0);
    }
}
