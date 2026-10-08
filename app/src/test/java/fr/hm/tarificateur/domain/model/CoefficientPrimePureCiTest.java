package fr.hm.tarificateur.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class CoefficientPrimePureCiTest {

    @Test
    void shouldCreateRecordWithValidValues() {
        Integer ageAdhesion = 35;
        Integer dureeEmpruntAnnees = 15;
        Double coefficient = 0.92;

        CoefficientPrimePureCi result = new CoefficientPrimePureCi(ageAdhesion, dureeEmpruntAnnees, coefficient);

        assertThat(result).isNotNull();
        assertThat(result.ageAdhesion()).isEqualTo(35);
        assertThat(result.dureeEmpruntAnnees()).isEqualTo(15);
        assertThat(result.coefficient()).isEqualTo(0.92);
    }

    @Test
    void shouldReturnCorrectGetterValues() {
        CoefficientPrimePureCi cppc = new CoefficientPrimePureCi(45, 20, 1.05);

        assertThat(cppc.ageAdhesion()).isEqualTo(45);
        assertThat(cppc.dureeEmpruntAnnees()).isEqualTo(20);
        assertThat(cppc.coefficient()).isEqualTo(1.05);
    }

    @Test
    void shouldBeEqualWhenSameValues() {
        CoefficientPrimePureCi cppc1 = new CoefficientPrimePureCi(35, 15, 0.92);
        CoefficientPrimePureCi cppc2 = new CoefficientPrimePureCi(35, 15, 0.92);

        assertThat(cppc1).isEqualTo(cppc2);
    }

    @Test
    void shouldNotBeEqualWhenDifferentValues() {
        CoefficientPrimePureCi cppc1 = new CoefficientPrimePureCi(35, 15, 0.92);
        CoefficientPrimePureCi cppc2 = new CoefficientPrimePureCi(45, 20, 1.05);

        assertThat(cppc1).isNotEqualTo(cppc2);
    }

    @Test
    void shouldHaveSameHashCodeForEqualObjects() {
        CoefficientPrimePureCi cppc1 = new CoefficientPrimePureCi(35, 15, 0.92);
        CoefficientPrimePureCi cppc2 = new CoefficientPrimePureCi(35, 15, 0.92);

        assertThat(cppc1).hasSameHashCodeAs(cppc2);
    }

    @Test
    void shouldContainFieldValuesInToString() {
        CoefficientPrimePureCi cppc = new CoefficientPrimePureCi(40, 25, 0.98);
        String toStringResult = cppc.toString();

        assertThat(toStringResult).contains("CoefficientPrimePureCi");
    }

    @Test
    void shouldHandleNullValues() {
        CoefficientPrimePureCi cppc = new CoefficientPrimePureCi(null, null, null);

        assertThat(cppc.ageAdhesion()).isNull();
        assertThat(cppc.dureeEmpruntAnnees()).isNull();
        assertThat(cppc.coefficient()).isNull();
    }

    @Test
    void shouldHandleZeroValues() {
        CoefficientPrimePureCi cppc = new CoefficientPrimePureCi(0, 0, 0.0);

        assertThat(cppc.ageAdhesion()).isEqualTo(0);
        assertThat(cppc.dureeEmpruntAnnees()).isEqualTo(0);
        assertThat(cppc.coefficient()).isEqualTo(0.0);
    }

    @Test
    void shouldHandleLargeValues() {
        CoefficientPrimePureCi cppc = new CoefficientPrimePureCi(100, 100, 999.99);

        assertThat(cppc.ageAdhesion()).isEqualTo(100);
        assertThat(cppc.dureeEmpruntAnnees()).isEqualTo(100);
        assertThat(cppc.coefficient()).isEqualTo(999.99);
    }
}

