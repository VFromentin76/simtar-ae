package fr.hm.tarificateur.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ObjetPretCoefficientTest {

    @Test
    void shouldCreateRecordWithValidValues() {
        Reference objetPret = new Reference("OP001", "Objet Pret 1");
        Boolean regimeLemoine = true;
        Double coefficient = 0.95;

        ObjetPretCoefficient result = new ObjetPretCoefficient(objetPret, regimeLemoine, coefficient);

        assertThat(result).isNotNull();
        assertThat(result.objetPret()).isEqualTo(objetPret);
        assertThat(result.regimeLemoine()).isTrue();
        assertThat(result.coefficient()).isEqualTo(0.95);
    }

    @Test
    void shouldReturnCorrectGetterValues() {
        Reference op = new Reference("OP002", "Objet Pret 2");
        ObjetPretCoefficient opc = new ObjetPretCoefficient(op, false, 1.05);

        assertThat(opc.objetPret()).isEqualTo(op);
        assertThat(opc.regimeLemoine()).isFalse();
        assertThat(opc.coefficient()).isEqualTo(1.05);
    }

    @Test
    void shouldBeEqualWhenSameValues() {
        Reference ref = new Reference("OP001", "Objet Pret 1");
        ObjetPretCoefficient opc1 = new ObjetPretCoefficient(ref, true, 0.95);
        ObjetPretCoefficient opc2 = new ObjetPretCoefficient(ref, true, 0.95);

        assertThat(opc1).isEqualTo(opc2);
    }

    @Test
    void shouldNotBeEqualWhenDifferentValues() {
        Reference ref1 = new Reference("OP001", "Objet Pret 1");
        Reference ref2 = new Reference("OP002", "Objet Pret 2");
        ObjetPretCoefficient opc1 = new ObjetPretCoefficient(ref1, true, 0.95);
        ObjetPretCoefficient opc2 = new ObjetPretCoefficient(ref2, false, 1.05);

        assertThat(opc1).isNotEqualTo(opc2);
    }

    @Test
    void shouldHaveSameHashCodeForEqualObjects() {
        Reference ref = new Reference("OP001", "Objet Pret 1");
        ObjetPretCoefficient opc1 = new ObjetPretCoefficient(ref, true, 0.95);
        ObjetPretCoefficient opc2 = new ObjetPretCoefficient(ref, true, 0.95);

        assertThat(opc1).hasSameHashCodeAs(opc2);
    }

    @Test
    void shouldHandleNullValues() {
        ObjetPretCoefficient opc = new ObjetPretCoefficient(null, null, null);

        assertThat(opc.objetPret()).isNull();
        assertThat(opc.regimeLemoine()).isNull();
        assertThat(opc.coefficient()).isNull();
    }

    @Test
    void shouldContainFieldValuesInToString() {
        Reference ref = new Reference("OP003", "Objet Pret 3");
        ObjetPretCoefficient opc = new ObjetPretCoefficient(ref, false, 1.1);
        String toStringResult = opc.toString();

        assertThat(toStringResult).contains("ObjetPretCoefficient");
    }
}

