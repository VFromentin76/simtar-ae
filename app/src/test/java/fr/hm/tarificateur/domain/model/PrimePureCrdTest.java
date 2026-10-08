package fr.hm.tarificateur.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class PrimePureCrdTest {

    @Test
    void shouldCreateRecordWithValidValues() {
        Integer ageAtteint = 60;
        Double coefficient = 1.15;

        PrimePureCrd result = new PrimePureCrd(ageAtteint, coefficient);

        assertThat(result).isNotNull();
        assertThat(result.ageAtteint()).isEqualTo(60);
        assertThat(result.coefficient()).isEqualTo(1.15);
    }

    @Test
    void shouldReturnCorrectGetterValues() {
        PrimePureCrd ppc = new PrimePureCrd(70, 1.35);

        assertThat(ppc.ageAtteint()).isEqualTo(70);
        assertThat(ppc.coefficient()).isEqualTo(1.35);
    }

    @Test
    void shouldBeEqualWhenSameValues() {
        PrimePureCrd ppc1 = new PrimePureCrd(60, 1.15);
        PrimePureCrd ppc2 = new PrimePureCrd(60, 1.15);

        assertThat(ppc1).isEqualTo(ppc2);
    }

    @Test
    void shouldNotBeEqualWhenDifferentValues() {
        PrimePureCrd ppc1 = new PrimePureCrd(60, 1.15);
        PrimePureCrd ppc2 = new PrimePureCrd(70, 1.35);

        assertThat(ppc1).isNotEqualTo(ppc2);
    }

    @Test
    void shouldHaveSameHashCodeForEqualObjects() {
        PrimePureCrd ppc1 = new PrimePureCrd(60, 1.15);
        PrimePureCrd ppc2 = new PrimePureCrd(60, 1.15);

        assertThat(ppc1).hasSameHashCodeAs(ppc2);
    }

    @Test
    void shouldContainFieldValuesInToString() {
        PrimePureCrd ppc = new PrimePureCrd(65, 1.25);
        String toStringResult = ppc.toString();

        assertThat(toStringResult).contains("PrimePureCrd");
    }

    @Test
    void shouldHandleNullValues() {
        PrimePureCrd ppc = new PrimePureCrd(null, null);

        assertThat(ppc.ageAtteint()).isNull();
        assertThat(ppc.coefficient()).isNull();
    }

    @Test
    void shouldHandleZeroAge() {
        PrimePureCrd ppc = new PrimePureCrd(0, 1.0);

        assertThat(ppc.ageAtteint()).isEqualTo(0);
        assertThat(ppc.coefficient()).isEqualTo(1.0);
    }

    @Test
    void shouldHandleLargeValues() {
        PrimePureCrd ppc = new PrimePureCrd(150, 999.99);

        assertThat(ppc.ageAtteint()).isEqualTo(150);
        assertThat(ppc.coefficient()).isEqualTo(999.99);
    }
}

