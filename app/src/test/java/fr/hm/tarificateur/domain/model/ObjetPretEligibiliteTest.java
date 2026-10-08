package fr.hm.tarificateur.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ObjetPretEligibiliteTest {

    @Test
    void shouldCreateRecordWithValidValues() {
        Reference objetPret = new Reference("OP001", "Objet Pret 1");
        Boolean regimeLemoine = true;
        Boolean booEligible = true;

        ObjetPretEligibilite result = new ObjetPretEligibilite(objetPret, regimeLemoine, booEligible);

        assertThat(result).isNotNull();
        assertThat(result.objetPret()).isEqualTo(objetPret);
        assertThat(result.regimeLemoine()).isTrue();
        assertThat(result.booEligible()).isTrue();
    }

    @Test
    void shouldReturnCorrectGetterValues() {
        Reference op = new Reference("OP002", "Objet Pret 2");
        ObjetPretEligibilite ope = new ObjetPretEligibilite(op, false, false);

        assertThat(ope.objetPret()).isEqualTo(op);
        assertThat(ope.regimeLemoine()).isFalse();
        assertThat(ope.booEligible()).isFalse();
    }

    @Test
    void shouldBeEqualWhenSameValues() {
        Reference ref = new Reference("OP001", "Objet Pret 1");
        ObjetPretEligibilite ope1 = new ObjetPretEligibilite(ref, true, true);
        ObjetPretEligibilite ope2 = new ObjetPretEligibilite(ref, true, true);

        assertThat(ope1).isEqualTo(ope2);
    }

    @Test
    void shouldNotBeEqualWhenDifferentValues() {
        Reference ref1 = new Reference("OP001", "Objet Pret 1");
        Reference ref2 = new Reference("OP002", "Objet Pret 2");
        ObjetPretEligibilite ope1 = new ObjetPretEligibilite(ref1, true, true);
        ObjetPretEligibilite ope2 = new ObjetPretEligibilite(ref2, false, false);

        assertThat(ope1).isNotEqualTo(ope2);
    }

    @Test
    void shouldHaveSameHashCodeForEqualObjects() {
        Reference ref = new Reference("OP001", "Objet Pret 1");
        ObjetPretEligibilite ope1 = new ObjetPretEligibilite(ref, true, true);
        ObjetPretEligibilite ope2 = new ObjetPretEligibilite(ref, true, true);

        assertThat(ope1).hasSameHashCodeAs(ope2);
    }

    @Test
    void shouldHandleNullValues() {
        ObjetPretEligibilite ope = new ObjetPretEligibilite(null, null, null);

        assertThat(ope.objetPret()).isNull();
        assertThat(ope.regimeLemoine()).isNull();
        assertThat(ope.booEligible()).isNull();
    }

    @Test
    void shouldContainFieldValuesInToString() {
        Reference ref = new Reference("OP003", "Objet Pret 3");
        ObjetPretEligibilite ope = new ObjetPretEligibilite(ref, true, false);
        String toStringResult = ope.toString();

        assertThat(toStringResult).contains("ObjetPretEligibilite");
    }
}

