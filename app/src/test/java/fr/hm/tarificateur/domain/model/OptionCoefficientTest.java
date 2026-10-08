package fr.hm.tarificateur.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class OptionCoefficientTest {

    @Test
    void shouldCreateRecordWithValidValues() {
        Reference option = new Reference("OPT001", "Option DecÃ¨s");
        Boolean regimeLemoine = true;
        Double coefficient = 1.1;

        OptionCoefficient result = new OptionCoefficient(option, regimeLemoine, coefficient);

        assertThat(result).isNotNull();
        assertThat(result.option()).isEqualTo(option);
        assertThat(result.regimeLemoine()).isTrue();
        assertThat(result.coefficient()).isEqualTo(1.1);
    }

    @Test
    void shouldReturnCorrectGetterValues() {
        Reference option = new Reference("OPT002", "Option Rente");
        OptionCoefficient oc = new OptionCoefficient(option, false, 0.95);

        assertThat(oc.option()).isEqualTo(option);
        assertThat(oc.regimeLemoine()).isFalse();
        assertThat(oc.coefficient()).isEqualTo(0.95);
    }

    @Test
    void shouldBeEqualWhenSameValues() {
        Reference ref = new Reference("OPT001", "Option DecÃ¨s");
        OptionCoefficient oc1 = new OptionCoefficient(ref, true, 1.1);
        OptionCoefficient oc2 = new OptionCoefficient(ref, true, 1.1);

        assertThat(oc1).isEqualTo(oc2);
    }

    @Test
    void shouldNotBeEqualWhenDifferentValues() {
        Reference ref1 = new Reference("OPT001", "Option DecÃ¨s");
        Reference ref2 = new Reference("OPT002", "Option Rente");
        OptionCoefficient oc1 = new OptionCoefficient(ref1, true, 1.1);
        OptionCoefficient oc2 = new OptionCoefficient(ref2, false, 0.95);

        assertThat(oc1).isNotEqualTo(oc2);
    }

    @Test
    void shouldHaveSameHashCodeForEqualObjects() {
        Reference ref = new Reference("OPT001", "Option DecÃ¨s");
        OptionCoefficient oc1 = new OptionCoefficient(ref, true, 1.1);
        OptionCoefficient oc2 = new OptionCoefficient(ref, true, 1.1);

        assertThat(oc1).hasSameHashCodeAs(oc2);
    }

    @Test
    void shouldContainFieldValuesInToString() {
        Reference ref = new Reference("OPT001", "Option DecÃ¨s");
        OptionCoefficient oc = new OptionCoefficient(ref, true, 1.1);
        String toStringResult = oc.toString();

        assertThat(toStringResult).contains("OptionCoefficient");
    }

    @Test
    void shouldHandleNullReference() {
        OptionCoefficient oc = new OptionCoefficient(null, true, 1.1);

        assertThat(oc.option()).isNull();
        assertThat(oc.regimeLemoine()).isTrue();
        assertThat(oc.coefficient()).isEqualTo(1.1);
    }

    @Test
    void shouldHandleNullCoefficient() {
        Reference ref = new Reference("OPT001", "Option DecÃ¨s");
        OptionCoefficient oc = new OptionCoefficient(ref, true, null);

        assertThat(oc.coefficient()).isNull();
    }

    @Test
    void shouldHandleNullRegimeLemoine() {
        Reference ref = new Reference("OPT001", "Option DecÃ¨s");
        OptionCoefficient oc = new OptionCoefficient(ref, null, 1.1);

        assertThat(oc.regimeLemoine()).isNull();
    }
}

