package fr.hm.tarificateur.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class CritereProTest {

    @Test
    void shouldCreateRecordWithValidValues() {
        Reference critereProfessionnel = new Reference("CRIT001", "Critere Professionnel 1");
        Boolean valeurAttendue = true;

        CriterePro result = new CriterePro(critereProfessionnel, valeurAttendue);

        assertThat(result).isNotNull();
        assertThat(result.critereProfessionnel()).isEqualTo(critereProfessionnel);
        assertThat(result.valeurAttendue()).isTrue();
    }

    @Test
    void shouldReturnCorrectGetterValues() {
        Reference ref = new Reference("CRIT002", "Critere Professionnel 2");
        CriterePro cp = new CriterePro(ref, false);

        assertThat(cp.critereProfessionnel()).isEqualTo(ref);
        assertThat(cp.valeurAttendue()).isFalse();
    }

    @Test
    void shouldBeEqualWhenSameValues() {
        Reference ref = new Reference("CRIT001", "Critere Professionnel 1");
        CriterePro cp1 = new CriterePro(ref, true);
        CriterePro cp2 = new CriterePro(ref, true);

        assertThat(cp1).isEqualTo(cp2);
    }

    @Test
    void shouldNotBeEqualWhenDifferentValues() {
        Reference ref1 = new Reference("CRIT001", "Critere Professionnel 1");
        Reference ref2 = new Reference("CRIT002", "Critere Professionnel 2");
        CriterePro cp1 = new CriterePro(ref1, true);
        CriterePro cp2 = new CriterePro(ref2, false);

        assertThat(cp1).isNotEqualTo(cp2);
    }

    @Test
    void shouldHaveSameHashCodeForEqualObjects() {
        Reference ref = new Reference("CRIT001", "Critere Professionnel 1");
        CriterePro cp1 = new CriterePro(ref, true);
        CriterePro cp2 = new CriterePro(ref, true);

        assertThat(cp1).hasSameHashCodeAs(cp2);
    }

    @Test
    void shouldHandleNullValues() {
        CriterePro cp = new CriterePro(null, null);

        assertThat(cp.critereProfessionnel()).isNull();
        assertThat(cp.valeurAttendue()).isNull();
    }

    @Test
    void shouldContainFieldValuesInToString() {
        Reference ref = new Reference("CRIT003", "Critere Professionnel 3");
        CriterePro cp = new CriterePro(ref, true);
        String toStringResult = cp.toString();

        assertThat(toStringResult).contains("CriterePro");
    }
}

