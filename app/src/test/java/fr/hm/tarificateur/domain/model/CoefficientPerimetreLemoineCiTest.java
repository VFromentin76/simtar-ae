package fr.hm.tarificateur.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class CoefficientPerimetreLemoineCiTest {

    @Test
    void shouldCreateRecordWithValidValues() {
        CoefficientPerimetreLemoineCi result = new CoefficientPerimetreLemoineCi(31, "VIE", 109.5);

        assertThat(result).isNotNull();
        assertThat(result.ageAdhesion()).isEqualTo(31);
        assertThat(result.branche()).isEqualTo("VIE");
        assertThat(result.coefficient()).isEqualTo(109.5);
    }

    @Test
    void shouldReturnCorrectGetterValues() {
        CoefficientPerimetreLemoineCi coeff = new CoefficientPerimetreLemoineCi(18, "NON_VIE", 123.0);

        assertThat(coeff.ageAdhesion()).isEqualTo(18);
        assertThat(coeff.branche()).isEqualTo("NON_VIE");
        assertThat(coeff.coefficient()).isEqualTo(123.0);
    }

    @Test
    void shouldBeEqualWhenSameValues() {
        CoefficientPerimetreLemoineCi coeff1 = new CoefficientPerimetreLemoineCi(31, "VIE", 109.5);
        CoefficientPerimetreLemoineCi coeff2 = new CoefficientPerimetreLemoineCi(31, "VIE", 109.5);

        assertThat(coeff1).isEqualTo(coeff2);
    }

    @Test
    void shouldNotBeEqualWhenDifferentValues() {
        CoefficientPerimetreLemoineCi coeff1 = new CoefficientPerimetreLemoineCi(31, "VIE", 109.5);
        CoefficientPerimetreLemoineCi coeff2 = new CoefficientPerimetreLemoineCi(18, "NON_VIE", 123.0);

        assertThat(coeff1).isNotEqualTo(coeff2);
    }

    @Test
    void shouldHaveSameHashCodeForEqualObjects() {
        CoefficientPerimetreLemoineCi coeff1 = new CoefficientPerimetreLemoineCi(31, "VIE", 109.5);
        CoefficientPerimetreLemoineCi coeff2 = new CoefficientPerimetreLemoineCi(31, "VIE", 109.5);

        assertThat(coeff1).hasSameHashCodeAs(coeff2);
    }

    @Test
    void shouldContainFieldValuesInToString() {
        CoefficientPerimetreLemoineCi coeff = new CoefficientPerimetreLemoineCi(31, "VIE", 109.5);

        assertThat(coeff.toString()).contains("CoefficientPerimetreLemoineCi");
    }

    @Test
    void shouldHandleNullValues() {
        CoefficientPerimetreLemoineCi coeff = new CoefficientPerimetreLemoineCi(null, null, null);

        assertThat(coeff.ageAdhesion()).isNull();
        assertThat(coeff.branche()).isNull();
        assertThat(coeff.coefficient()).isNull();
    }
}
