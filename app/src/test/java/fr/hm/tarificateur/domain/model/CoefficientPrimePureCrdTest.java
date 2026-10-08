package fr.hm.tarificateur.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class CoefficientPrimePureCrdTest {

    @Test
    void shouldCreateRecordWithValidValues() {
        Integer ageAtteint = 60;
        Double coefficient = 1.15;

        CoefficientPrimePureCrd result = new CoefficientPrimePureCrd(ageAtteint, coefficient);

        assertThat(result).isNotNull();
        assertThat(result.ageAtteint()).isEqualTo(60);
        assertThat(result.coefficient()).isEqualTo(1.15);
    }

    @Test
    void shouldReturnCorrectGetterValues() {
        CoefficientPrimePureCrd cppc = new CoefficientPrimePureCrd(70, 1.35);

        assertThat(cppc.ageAtteint()).isEqualTo(70);
        assertThat(cppc.coefficient()).isEqualTo(1.35);
    }

    @Test
    void shouldBeEqualWhenSameValues() {
        CoefficientPrimePureCrd cppc1 = new CoefficientPrimePureCrd(60, 1.15);
        CoefficientPrimePureCrd cppc2 = new CoefficientPrimePureCrd(60, 1.15);

        assertThat(cppc1).isEqualTo(cppc2);
    }

    @Test
    void shouldNotBeEqualWhenDifferentValues() {
        CoefficientPrimePureCrd cppc1 = new CoefficientPrimePureCrd(60, 1.15);
        CoefficientPrimePureCrd cppc2 = new CoefficientPrimePureCrd(70, 1.35);

        assertThat(cppc1).isNotEqualTo(cppc2);
    }

    @Test
    void shouldHaveSameHashCodeForEqualObjects() {
        CoefficientPrimePureCrd cppc1 = new CoefficientPrimePureCrd(60, 1.15);
        CoefficientPrimePureCrd cppc2 = new CoefficientPrimePureCrd(60, 1.15);

        assertThat(cppc1).hasSameHashCodeAs(cppc2);
    }

    @Test
    void shouldContainFieldValuesInToString() {
        CoefficientPrimePureCrd cppc = new CoefficientPrimePureCrd(65, 1.25);
        String toStringResult = cppc.toString();

        assertThat(toStringResult).contains("CoefficientPrimePureCrd");
    }

    @Test
    void shouldHandleNullValues() {
        CoefficientPrimePureCrd cppc = new CoefficientPrimePureCrd(null, null);

        assertThat(cppc.ageAtteint()).isNull();
        assertThat(cppc.coefficient()).isNull();
    }

    @Test
    void shouldHandleZeroAge() {
        CoefficientPrimePureCrd cppc = new CoefficientPrimePureCrd(0, 1.0);

        assertThat(cppc.ageAtteint()).isEqualTo(0);
        assertThat(cppc.coefficient()).isEqualTo(1.0);
    }

    @Test
    void shouldHandleLargeValues() {
        CoefficientPrimePureCrd cppc = new CoefficientPrimePureCrd(150, 999.99);

        assertThat(cppc.ageAtteint()).isEqualTo(150);
        assertThat(cppc.coefficient()).isEqualTo(999.99);
    }
}

