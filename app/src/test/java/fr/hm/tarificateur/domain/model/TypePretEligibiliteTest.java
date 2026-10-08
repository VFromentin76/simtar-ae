package fr.hm.tarificateur.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class TypePretEligibiliteTest {

    @Test
    void shouldCreateRecordWithValidValues() {
        Reference typePret = new Reference("TP001", "Type Pret 1");
        Boolean regimeLemoine = true;
        Boolean booEligible = true;

        TypePretEligibilite result = new TypePretEligibilite(typePret, regimeLemoine, booEligible);

        assertThat(result).isNotNull();
        assertThat(result.typePret()).isEqualTo(typePret);
        assertThat(result.regimeLemoine()).isTrue();
        assertThat(result.booEligible()).isTrue();
    }

    @Test
    void shouldReturnCorrectGetterValues() {
        Reference tp = new Reference("TP002", "Type Pret 2");
        TypePretEligibilite tpe = new TypePretEligibilite(tp, false, false);

        assertThat(tpe.typePret()).isEqualTo(tp);
        assertThat(tpe.regimeLemoine()).isFalse();
        assertThat(tpe.booEligible()).isFalse();
    }

    @Test
    void shouldBeEqualWhenSameValues() {
        Reference ref = new Reference("TP001", "Type Pret 1");
        TypePretEligibilite tpe1 = new TypePretEligibilite(ref, true, true);
        TypePretEligibilite tpe2 = new TypePretEligibilite(ref, true, true);

        assertThat(tpe1).isEqualTo(tpe2);
    }

    @Test
    void shouldNotBeEqualWhenDifferentValues() {
        Reference ref1 = new Reference("TP001", "Type Pret 1");
        Reference ref2 = new Reference("TP002", "Type Pret 2");
        TypePretEligibilite tpe1 = new TypePretEligibilite(ref1, true, true);
        TypePretEligibilite tpe2 = new TypePretEligibilite(ref2, false, false);

        assertThat(tpe1).isNotEqualTo(tpe2);
    }

    @Test
    void shouldHaveSameHashCodeForEqualObjects() {
        Reference ref = new Reference("TP001", "Type Pret 1");
        TypePretEligibilite tpe1 = new TypePretEligibilite(ref, true, true);
        TypePretEligibilite tpe2 = new TypePretEligibilite(ref, true, true);

        assertThat(tpe1).hasSameHashCodeAs(tpe2);
    }

    @Test
    void shouldHandleNullValues() {
        TypePretEligibilite tpe = new TypePretEligibilite(null, null, null);

        assertThat(tpe.typePret()).isNull();
        assertThat(tpe.regimeLemoine()).isNull();
        assertThat(tpe.booEligible()).isNull();
    }

    @Test
    void shouldContainFieldValuesInToString() {
        Reference ref = new Reference("TP003", "Type Pret 3");
        TypePretEligibilite tpe = new TypePretEligibilite(ref, true, false);
        String toStringResult = tpe.toString();

        assertThat(toStringResult).contains("TypePretEligibilite");
    }
}

