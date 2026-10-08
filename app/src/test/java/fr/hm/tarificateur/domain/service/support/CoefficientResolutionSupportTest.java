package fr.hm.tarificateur.domain.service.support;

import fr.hm.tarificateur.domain.model.CategorieProClasseRisqueMapping;
import fr.hm.tarificateur.domain.model.CoefficientPassageFumeurCi;
import fr.hm.tarificateur.domain.model.ProfessionClasseRisqueMapping;
import fr.hm.tarificateur.domain.model.Reference;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class CoefficientResolutionSupportTest {

    @Test
    void shouldNormalizeBrancheToUpperCaseWithUnderscore() {
        assertThat(CoefficientResolutionSupport.normalizeBranche(" non-vie "))
            .isEqualTo("NON_VIE");
        assertThat(CoefficientResolutionSupport.normalizeBranche("VIE")).isEqualTo("VIE");
    }

    @Test
    void shouldReturnNullWhenNormalizingNullBranche() {
        assertThat(CoefficientResolutionSupport.normalizeBranche(null)).isNull();
    }

    @Test
    void shouldApplyMultiplierWhenPresent() {
        double result = CoefficientResolutionSupport.applyMultiplier(100.0, 110.0);
        assertThat(result).isCloseTo(110.0, within(0.001));
    }

    @Test
    void shouldIgnoreNullMultiplier() {
        double result = CoefficientResolutionSupport.applyMultiplier(100.0, null);
        assertThat(result).isEqualTo(100.0);
    }

    @Test
    void shouldRoundToTwoDecimalsHalfUp() {
        assertThat(CoefficientResolutionSupport.round(1.005)).isEqualTo(1.01);
        assertThat(CoefficientResolutionSupport.round(1.004)).isEqualTo(1.00);
    }

    @Test
    void shouldResolveExactAgeAndBrancheMatch() {
        List<CoefficientPassageFumeurCi> coefficients = List.of(
            new CoefficientPassageFumeurCi(30, "NON_VIE", 120.0),
            new CoefficientPassageFumeurCi(45, "NON_VIE", 150.0)
        );

        Double result = CoefficientResolutionSupport.resolveByAgeAndBranche(
            coefficients, 45, "NON_VIE",
            CoefficientPassageFumeurCi::ageAdhesion, CoefficientPassageFumeurCi::branche,
            CoefficientPassageFumeurCi::coefficient);

        assertThat(result).isEqualTo(150.0);
    }

    @Test
    void shouldFallbackToClosestAgeWhenNoExactMatch() {
        List<CoefficientPassageFumeurCi> coefficients = List.of(
            new CoefficientPassageFumeurCi(30, "NON_VIE", 120.0),
            new CoefficientPassageFumeurCi(50, "NON_VIE", 150.0)
        );

        Double result = CoefficientResolutionSupport.resolveByAgeAndBranche(
            coefficients, 45, "NON_VIE",
            CoefficientPassageFumeurCi::ageAdhesion, CoefficientPassageFumeurCi::branche,
            CoefficientPassageFumeurCi::coefficient);

        assertThat(result).isEqualTo(150.0);
    }

    @Test
    void shouldReturnNullWhenBrancheDoesNotMatchAnyEntry() {
        List<CoefficientPassageFumeurCi> coefficients = List.of(
            new CoefficientPassageFumeurCi(45, "VIE", 150.0)
        );

        Double result = CoefficientResolutionSupport.resolveByAgeAndBranche(
            coefficients, 45, "NON_VIE",
            CoefficientPassageFumeurCi::ageAdhesion, CoefficientPassageFumeurCi::branche,
            CoefficientPassageFumeurCi::coefficient);

        assertThat(result).isNull();
    }

    @Test
    void shouldReturnNullWhenCoefficientsListIsNullOrEmpty() {
        assertThat(CoefficientResolutionSupport.resolveByAgeAndBranche(
            null, 45, "NON_VIE",
            CoefficientPassageFumeurCi::ageAdhesion, CoefficientPassageFumeurCi::branche,
            CoefficientPassageFumeurCi::coefficient)).isNull();

        assertThat(CoefficientResolutionSupport.resolveByAgeAndBranche(
            List.of(), 45, "NON_VIE",
            CoefficientPassageFumeurCi::ageAdhesion, CoefficientPassageFumeurCi::branche,
            CoefficientPassageFumeurCi::coefficient)).isNull();
    }

    @Test
    void shouldReturnNullWhenAgeAdhesionIsNull() {
        List<CoefficientPassageFumeurCi> coefficients = List.of(
            new CoefficientPassageFumeurCi(45, "NON_VIE", 150.0)
        );

        assertThat(CoefficientResolutionSupport.resolveByAgeAndBranche(
            coefficients, null, "NON_VIE",
            CoefficientPassageFumeurCi::ageAdhesion, CoefficientPassageFumeurCi::branche,
            CoefficientPassageFumeurCi::coefficient)).isNull();
    }

    @Test
    void shouldResolveWithoutBrancheFilterWhenBrancheIsNull() {
        List<CoefficientPassageFumeurCi> coefficients = List.of(
            new CoefficientPassageFumeurCi(45, "VIE", 130.0)
        );

        Double result = CoefficientResolutionSupport.resolveByAgeAndBranche(
            coefficients, 45, null,
            CoefficientPassageFumeurCi::ageAdhesion, CoefficientPassageFumeurCi::branche,
            CoefficientPassageFumeurCi::coefficient);

        assertThat(result).isEqualTo(130.0);
    }

    @Test
    void shouldResolveClasseRisqueCodeFromProfessionMapping() {
        List<ProfessionClasseRisqueMapping> professionMappings = List.of(
            new ProfessionClasseRisqueMapping(new Reference("2", "Développeur"), "VIE",
                new Reference("CR1", "Classe risque 1"))
        );

        String result = CoefficientResolutionSupport.resolveClasseRisqueCode("2", professionMappings, List.of());

        assertThat(result).isEqualTo("CR1");
    }

    @Test
    void shouldResolveProfessionRiskClassForRequestedBranch() {
        List<ProfessionClasseRisqueMapping> professionMappings = List.of(
            new ProfessionClasseRisqueMapping(new Reference("2", "Développeur"), "VIE",
                new Reference("DC1", "Décès")),
            new ProfessionClasseRisqueMapping(new Reference("2", "Développeur"), "NON_VIE",
                new Reference("AT2", "Arrêt de travail"))
        );

        assertThat(CoefficientResolutionSupport.resolveClasseRisqueCode(
            "2", "vie", professionMappings, List.of())).isEqualTo("DC1");
        assertThat(CoefficientResolutionSupport.resolveClasseRisqueCode(
            "2", "non-vie", professionMappings, List.of())).isEqualTo("AT2");
    }

    @Test
    void shouldResolveCategoryRiskClassForRequestedBranch() {
        List<CategorieProClasseRisqueMapping> categorieMappings = List.of(
            new CategorieProClasseRisqueMapping(
                new Reference("CAT1", "Catégorie 1"),
                new Reference("DC1", "Décès"),
                new Reference("AT2", "Arrêt de travail"),
                List.of())
        );

        assertThat(CoefficientResolutionSupport.resolveClasseRisqueCode(
            "CAT1", "VIE", List.of(), categorieMappings)).isEqualTo("DC1");
        assertThat(CoefficientResolutionSupport.resolveClasseRisqueCode(
            "CAT1", "NON_VIE", List.of(), categorieMappings)).isEqualTo("AT2");
    }

    @Test
    void shouldFallbackToCategorieProMappingWhenNoProfessionMatch() {
        List<CategorieProClasseRisqueMapping> categorieMappings = List.of(
            new CategorieProClasseRisqueMapping(new Reference("CAT1", "Catégorie 1"),
                new Reference("CR2", "Classe risque 2"), null)
        );

        String result = CoefficientResolutionSupport.resolveClasseRisqueCode(
            "CAT1", List.of(), categorieMappings);

        assertThat(result).isEqualTo("CR2");
    }

    @Test
    void shouldReturnNullWhenCspCodeIsNull() {
        assertThat(CoefficientResolutionSupport.resolveClasseRisqueCode(null, List.of(), List.of())).isNull();
    }

    @Test
    void shouldReturnNullWhenNoMappingMatches() {
        assertThat(CoefficientResolutionSupport.resolveClasseRisqueCode("UNKNOWN", List.of(), List.of())).isNull();
    }
}
