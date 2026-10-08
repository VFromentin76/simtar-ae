package fr.hm.tarificateur.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class PrimePureCiTest {

    @Test
    void shouldCreateRecordWithValidValues() {
        Integer ageAdhesion = 35;
        Integer dureePretAnnees = 15;
        Double coefficient = 0.92;

        PrimePureCi result = new PrimePureCi(ageAdhesion, dureePretAnnees, coefficient);

        assertThat(result).isNotNull();
        assertThat(result.ageAdhesion()).isEqualTo(35);
        assertThat(result.dureePretAnnees()).isEqualTo(15);
        assertThat(result.coefficient()).isEqualTo(0.92);
    }

    @Test
    void shouldReturnCorrectGetterValues() {
        PrimePureCi ppc = new PrimePureCi(45, 20, 1.05);

        assertThat(ppc.ageAdhesion()).isEqualTo(45);
        assertThat(ppc.dureePretAnnees()).isEqualTo(20);
        assertThat(ppc.coefficient()).isEqualTo(1.05);
    }

    @Test
    void shouldBeEqualWhenSameValues() {
        PrimePureCi ppc1 = new PrimePureCi(35, 15, 0.92);
        PrimePureCi ppc2 = new PrimePureCi(35, 15, 0.92);

        assertThat(ppc1).isEqualTo(ppc2);
    }

    @Test
    void shouldNotBeEqualWhenDifferentValues() {
        PrimePureCi ppc1 = new PrimePureCi(35, 15, 0.92);
        PrimePureCi ppc2 = new PrimePureCi(45, 20, 1.05);

        assertThat(ppc1).isNotEqualTo(ppc2);
    }

    @Test
    void shouldHaveSameHashCodeForEqualObjects() {
        PrimePureCi ppc1 = new PrimePureCi(35, 15, 0.92);
        PrimePureCi ppc2 = new PrimePureCi(35, 15, 0.92);

        assertThat(ppc1).hasSameHashCodeAs(ppc2);
    }

    @Test
    void shouldContainFieldValuesInToString() {
        PrimePureCi ppc = new PrimePureCi(40, 25, 0.98);
        String toStringResult = ppc.toString();

        assertThat(toStringResult).contains("PrimePureCi");
    }

    @Test
    void shouldHandleNullValues() {
        PrimePureCi ppc = new PrimePureCi(null, null, null);

        assertThat(ppc.ageAdhesion()).isNull();
        assertThat(ppc.dureePretAnnees()).isNull();
        assertThat(ppc.coefficient()).isNull();
    }

    @Test
    void shouldHandleZeroValues() {
        PrimePureCi ppc = new PrimePureCi(0, 0, 0.0);

        assertThat(ppc.ageAdhesion()).isEqualTo(0);
        assertThat(ppc.dureePretAnnees()).isEqualTo(0);
        assertThat(ppc.coefficient()).isEqualTo(0.0);
    }

    @Test
    void shouldHandleLargeValues() {
        PrimePureCi ppc = new PrimePureCi(100, 100, 999.99);

        assertThat(ppc.ageAdhesion()).isEqualTo(100);
        assertThat(ppc.dureePretAnnees()).isEqualTo(100);
        assertThat(ppc.coefficient()).isEqualTo(999.99);
    }
}

