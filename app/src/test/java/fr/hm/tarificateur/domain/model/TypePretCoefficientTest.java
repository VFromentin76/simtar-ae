package fr.hm.tarificateur.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class TypePretCoefficientTest {

    @Test
    void shouldCreateRecordWithValidValues() {
        Reference typePret = new Reference("TP001", "Type Pret 1");
        String branche = "CI";
        Boolean regimeLemoine = true;
        Double coefficient = 0.95;

        TypePretCoefficient result = new TypePretCoefficient(typePret, branche, regimeLemoine, coefficient);

        assertThat(result).isNotNull();
        assertThat(result.typePret()).isEqualTo(typePret);
        assertThat(result.branche()).isEqualTo("CI");
        assertThat(result.regimeLemoine()).isTrue();
        assertThat(result.coefficient()).isEqualTo(0.95);
    }

    @Test
    void shouldReturnCorrectGetterValues() {
        Reference tp = new Reference("TP002", "Type Pret 2");
        TypePretCoefficient tpc = new TypePretCoefficient(tp, "CRD", false, 1.05);

        assertThat(tpc.typePret()).isEqualTo(tp);
        assertThat(tpc.branche()).isEqualTo("CRD");
        assertThat(tpc.regimeLemoine()).isFalse();
        assertThat(tpc.coefficient()).isEqualTo(1.05);
    }

    @Test
    void shouldBeEqualWhenSameValues() {
        Reference ref = new Reference("TP001", "Type Pret 1");
        TypePretCoefficient tpc1 = new TypePretCoefficient(ref, "CI", true, 0.95);
        TypePretCoefficient tpc2 = new TypePretCoefficient(ref, "CI", true, 0.95);

        assertThat(tpc1).isEqualTo(tpc2);
    }

    @Test
    void shouldNotBeEqualWhenDifferentValues() {
        Reference ref1 = new Reference("TP001", "Type Pret 1");
        Reference ref2 = new Reference("TP002", "Type Pret 2");
        TypePretCoefficient tpc1 = new TypePretCoefficient(ref1, "CI", true, 0.95);
        TypePretCoefficient tpc2 = new TypePretCoefficient(ref2, "CRD", false, 1.05);

        assertThat(tpc1).isNotEqualTo(tpc2);
    }

    @Test
    void shouldHaveSameHashCodeForEqualObjects() {
        Reference ref = new Reference("TP001", "Type Pret 1");
        TypePretCoefficient tpc1 = new TypePretCoefficient(ref, "CI", true, 0.95);
        TypePretCoefficient tpc2 = new TypePretCoefficient(ref, "CI", true, 0.95);

        assertThat(tpc1).hasSameHashCodeAs(tpc2);
    }

    @Test
    void shouldHandleNullValues() {
        TypePretCoefficient tpc = new TypePretCoefficient(null, null, null, null);

        assertThat(tpc.typePret()).isNull();
        assertThat(tpc.branche()).isNull();
        assertThat(tpc.regimeLemoine()).isNull();
        assertThat(tpc.coefficient()).isNull();
    }

    @Test
    void shouldContainFieldValuesInToString() {
        Reference ref = new Reference("TP003", "Type Pret 3");
        TypePretCoefficient tpc = new TypePretCoefficient(ref, "OTHER", true, 1.1);
        String toStringResult = tpc.toString();

        assertThat(toStringResult).contains("TypePretCoefficient");
    }
}

