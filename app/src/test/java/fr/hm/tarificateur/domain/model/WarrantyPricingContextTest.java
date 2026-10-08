package fr.hm.tarificateur.domain.model;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WarrantyPricingContextTest {

    private WarrantyPricingContext sample() {
        DetailConfig detailConfig = new DetailConfig(
                "CONTRAT", "ASSOC", null, null, null, "01/01", "01/01",
                true, null, 0.05, 0.03, 0.02, 0.01, 10.0, 15.0,
                0.9, 500000.0, 1.1, 0.95
        );
        List<TypePretCoefficient> typePret = List.of(
                new TypePretCoefficient(new Reference("PRET_AMORTISSABLE", "Prêt"), "VIE", false, 120.0)
        );
        List<ObjetPretCoefficient> objetPret = List.of(
                new ObjetPretCoefficient(new Reference("RESIDENCE_PRINCIPALE", "RP"), false, 90.0)
        );
        List<ClasseRisqueCoefficient> classeRisque = List.of(
                new ClasseRisqueCoefficient(new Reference("CSP1", "Cadres"), "VIE", false, 95.0)
        );
        List<CoefficientPassageFumeurCi> fumeur = List.of(
                new CoefficientPassageFumeurCi(45, "VIE", 150.0)
        );
        List<ProfessionClasseRisqueMapping> professionMappings = List.of(
                new ProfessionClasseRisqueMapping(new Reference("2", "Dev"), "VIE",
                        new Reference("CSP1", "Cadres"))
        );

        return new WarrantyPricingContext(
                detailConfig, true, false, true,
                "PRET_AMORTISSABLE", "RESIDENCE_PRINCIPALE", "2", 200000.0,
                typePret, objetPret, classeRisque, fumeur,
                Collections.emptyList(), professionMappings, "FR_90J"
        );
    }

    @Test
    void shouldCreateRecordWithValidValues() {
        WarrantyPricingContext ctx = sample();

        assertThat(ctx).isNotNull();
        assertThat(ctx.couple()).isTrue();
        assertThat(ctx.smoker()).isFalse();
        assertThat(ctx.lemoineProfile()).isTrue();
        assertThat(ctx.typePret()).isEqualTo("PRET_AMORTISSABLE");
        assertThat(ctx.objetPret()).isEqualTo("RESIDENCE_PRINCIPALE");
        assertThat(ctx.cspCode()).isEqualTo("2");
        assertThat(ctx.loanInsuredCapitalNonVie()).isEqualTo(200000.0);
        assertThat(ctx.franchiseCode()).isEqualTo("FR_90J");
    }

    @Test
    void shouldReturnCorrectAccessorValues() {
        WarrantyPricingContext ctx = sample();

        assertThat(ctx.detailConfig()).isNotNull();
        assertThat(ctx.typePretCoefficients()).hasSize(1);
        assertThat(ctx.objetPretCoefficients()).hasSize(1);
        assertThat(ctx.classeRisqueCoefficients()).hasSize(1);
        assertThat(ctx.coefficientsPassageFumeurCi()).hasSize(1);
        assertThat(ctx.mappingsCategoriePro()).isEmpty();
        assertThat(ctx.mappingsProfession()).hasSize(1);
    }

    @Test
    void shouldBuildEmptyContextWithAllNullFields() {
        WarrantyPricingContext empty = WarrantyPricingContext.empty();

        assertThat(empty).isNotNull();
        assertThat(empty.detailConfig()).isNull();
        assertThat(empty.couple()).isNull();
        assertThat(empty.smoker()).isNull();
        assertThat(empty.lemoineProfile()).isNull();
        assertThat(empty.typePret()).isNull();
        assertThat(empty.objetPret()).isNull();
        assertThat(empty.cspCode()).isNull();
        assertThat(empty.loanInsuredCapitalNonVie()).isNull();
        assertThat(empty.typePretCoefficients()).isNull();
        assertThat(empty.objetPretCoefficients()).isNull();
        assertThat(empty.classeRisqueCoefficients()).isNull();
        assertThat(empty.coefficientsPassageFumeurCi()).isNull();
        assertThat(empty.mappingsCategoriePro()).isNull();
        assertThat(empty.mappingsProfession()).isNull();
        assertThat(empty.franchiseCode()).isNull();
    }

    @Test
    void shouldBeEqualWhenSameValues() {
        WarrantyPricingContext a = WarrantyPricingContext.empty();
        WarrantyPricingContext b = WarrantyPricingContext.empty();

        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
    }

    @Test
    void shouldNotBeEqualWhenValuesDiffer() {
        WarrantyPricingContext a = sample();
        WarrantyPricingContext b = WarrantyPricingContext.empty();

        assertThat(a).isNotEqualTo(b);
    }

    @Test
    void shouldExposeKeyFieldsInToString() {
        WarrantyPricingContext ctx = sample();

        assertThat(ctx.toString())
                .contains("PRET_AMORTISSABLE")
                .contains("RESIDENCE_PRINCIPALE");
    }
}
