package fr.hm.tarificateur.domain.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExtendedProductConfigurationModelsTest {

    @Test
    void shouldExposeCiAndCrdAerasCoefficients() {
        CoefficientAerasCi ci = new CoefficientAerasCi(42, 20, 125.5);
        CoefficientAerasCrd crd = new CoefficientAerasCrd(57, 130.0);

        assertThat(ci)
            .isEqualTo(new CoefficientAerasCi(42, 20, 125.5))
            .hasSameHashCodeAs(new CoefficientAerasCi(42, 20, 125.5));
        assertThat(ci.ageAdhesion()).isEqualTo(42);
        assertThat(ci.dureePretAnnees()).isEqualTo(20);
        assertThat(ci.coefficient()).isEqualTo(125.5);
        assertThat(crd.ageAtteint()).isEqualTo(57);
        assertThat(crd.coefficient()).isEqualTo(130.0);
        assertThat(ci.toString()).contains("42", "20", "125.5");
        assertThat(crd.toString()).contains("57", "130.0");
    }

    @Test
    void shouldExposeCrdLemoineAndLoanCurveConfiguration() {
        CoefficientPerimetreLemoineCrd coefficient =
            new CoefficientPerimetreLemoineCrd(35, "NON_VIE", 110.0);
        TypePretCourbeDeformationCrd curve = new TypePretCourbeDeformationCrd(
            new Reference("AMORTISSABLE", "Amortissable"), true);

        assertThat(coefficient.ageAdhesion()).isEqualTo(35);
        assertThat(coefficient.branche()).isEqualTo("NON_VIE");
        assertThat(coefficient.coefficient()).isEqualTo(110.0);
        assertThat(curve.typePret().code()).isEqualTo("AMORTISSABLE");
        assertThat(curve.booCourbeCrdApplicable()).isTrue();
        assertThat(coefficient.toString()).contains("NON_VIE");
        assertThat(curve).isEqualTo(new TypePretCourbeDeformationCrd(
            new Reference("AMORTISSABLE", "Amortissable"), true));
    }

    @Test
    void shouldExposeProfessionalExclusionConfiguration() {
        ExclusionCategoriePro exclusion = new ExclusionCategoriePro(
            new Reference("RISQUE", "Profession à risque"), true, false);

        assertThat(exclusion.categorieProfessionnelle().code()).isEqualTo("RISQUE");
        assertThat(exclusion.exclueLemoine()).isTrue();
        assertThat(exclusion.exclueGarantieNonVie()).isFalse();
        assertThat(exclusion.toString()).contains("RISQUE", "true", "false");
    }

    @Test
    void shouldExposePremiumWaiverConfiguration() {
        ExonerationCotisationsCoefficient lemoine =
            new ExonerationCotisationsCoefficient(true, 105.0);
        ExonerationCotisationsCoefficient horsLemoine =
            new ExonerationCotisationsCoefficient(false, 115.0);
        ExonerationCotisations exoneration =
            new ExonerationCotisations(false, List.of(lemoine, horsLemoine));

        assertThat(exoneration.exclueLemoine()).isFalse();
        assertThat(exoneration.coefficients()).containsExactly(lemoine, horsLemoine);
        assertThat(lemoine.regimeLemoine()).isTrue();
        assertThat(lemoine.coefficient()).isEqualTo(105.0);
        assertThat(exoneration).isEqualTo(
            new ExonerationCotisations(false, List.of(lemoine, horsLemoine)));
        assertThat(exoneration.toString()).contains("105.0", "115.0");
    }
}
