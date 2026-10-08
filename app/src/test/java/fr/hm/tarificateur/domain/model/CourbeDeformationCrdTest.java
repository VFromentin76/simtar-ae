package fr.hm.tarificateur.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class CourbeDeformationCrdTest {

    @Test
    void shouldCreateRecordWithValidValues() {
        Integer anciennetePretAnnees = 5;
        Integer dureePretAnnees = 15;
        Double coefficient = 1.05;

        CourbeDeformationCrd result = new CourbeDeformationCrd(anciennetePretAnnees, dureePretAnnees, coefficient);

        assertThat(result).isNotNull();
        assertThat(result.anciennetePretAnnees()).isEqualTo(5);
        assertThat(result.dureePretAnnees()).isEqualTo(15);
        assertThat(result.coefficient()).isEqualTo(1.05);
    }

    @Test
    void shouldReturnCorrectGetterValues() {
        CourbeDeformationCrd curve = new CourbeDeformationCrd(3, 20, 0.98);

        assertThat(curve.anciennetePretAnnees()).isEqualTo(3);
        assertThat(curve.dureePretAnnees()).isEqualTo(20);
        assertThat(curve.coefficient()).isEqualTo(0.98);
    }

    @Test
    void shouldBeEqualWhenSameValues() {
        CourbeDeformationCrd curve1 = new CourbeDeformationCrd(5, 15, 1.05);
        CourbeDeformationCrd curve2 = new CourbeDeformationCrd(5, 15, 1.05);

        assertThat(curve1).isEqualTo(curve2);
    }

    @Test
    void shouldNotBeEqualWhenDifferentValues() {
        CourbeDeformationCrd curve1 = new CourbeDeformationCrd(5, 15, 1.05);
        CourbeDeformationCrd curve2 = new CourbeDeformationCrd(3, 20, 0.98);

        assertThat(curve1).isNotEqualTo(curve2);
    }

    @Test
    void shouldHaveSameHashCodeForEqualObjects() {
        CourbeDeformationCrd curve1 = new CourbeDeformationCrd(5, 15, 1.05);
        CourbeDeformationCrd curve2 = new CourbeDeformationCrd(5, 15, 1.05);

        assertThat(curve1).hasSameHashCodeAs(curve2);
    }

    @Test
    void shouldContainFieldValuesInToString() {
        CourbeDeformationCrd curve = new CourbeDeformationCrd(7, 25, 1.02);
        String toStringResult = curve.toString();

        assertThat(toStringResult).contains("CourbeDeformationCrd");
    }

    @Test
    void shouldHandleNullValues() {
        CourbeDeformationCrd curve = new CourbeDeformationCrd(null, null, null);

        assertThat(curve.anciennetePretAnnees()).isNull();
        assertThat(curve.dureePretAnnees()).isNull();
        assertThat(curve.coefficient()).isNull();
    }

    @Test
    void shouldHandleZeroValues() {
        CourbeDeformationCrd curve = new CourbeDeformationCrd(0, 0, 0.0);

        assertThat(curve.anciennetePretAnnees()).isEqualTo(0);
        assertThat(curve.dureePretAnnees()).isEqualTo(0);
        assertThat(curve.coefficient()).isEqualTo(0.0);
    }

    @Test
    void shouldHandleLargeValues() {
        CourbeDeformationCrd curve = new CourbeDeformationCrd(100, 500, 99.99);

        assertThat(curve.anciennetePretAnnees()).isEqualTo(100);
        assertThat(curve.dureePretAnnees()).isEqualTo(500);
        assertThat(curve.coefficient()).isEqualTo(99.99);
    }
}

