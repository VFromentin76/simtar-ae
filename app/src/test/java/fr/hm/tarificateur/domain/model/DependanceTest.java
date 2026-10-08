package fr.hm.tarificateur.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class DependanceTest {

    @Test
    void shouldCreateRecordWithValidValues() {
        Reference garantieRequise = new Reference("GAR001", "Garantie Requise");

        Dependance result = new Dependance(garantieRequise);

        assertThat(result).isNotNull();
        assertThat(result.garantieRequise()).isEqualTo(garantieRequise);
    }

    @Test
    void shouldReturnCorrectGetterValue() {
        Reference reference = new Reference("GAR002", "Another Garantie");
        Dependance dependance = new Dependance(reference);

        assertThat(dependance.garantieRequise()).isEqualTo(reference);
        assertThat(dependance.garantieRequise().code()).isEqualTo("GAR002");
        assertThat(dependance.garantieRequise().libelle()).isEqualTo("Another Garantie");
    }

    @Test
    void shouldBeEqualWhenSameValues() {
        Reference ref = new Reference("GAR001", "Garantie");
        Dependance dep1 = new Dependance(ref);
        Dependance dep2 = new Dependance(ref);

        assertThat(dep1).isEqualTo(dep2);
    }

    @Test
    void shouldNotBeEqualWhenDifferentValues() {
        Reference ref1 = new Reference("GAR001", "Garantie1");
        Reference ref2 = new Reference("GAR002", "Garantie2");
        Dependance dep1 = new Dependance(ref1);
        Dependance dep2 = new Dependance(ref2);

        assertThat(dep1).isNotEqualTo(dep2);
    }

    @Test
    void shouldHaveSameHashCodeForEqualObjects() {
        Reference ref = new Reference("GAR001", "Garantie");
        Dependance dep1 = new Dependance(ref);
        Dependance dep2 = new Dependance(ref);

        assertThat(dep1).hasSameHashCodeAs(dep2);
    }

    @Test
    void shouldHaveDifferentHashCodeForDifferentObjects() {
        Reference ref1 = new Reference("GAR001", "Garantie1");
        Reference ref2 = new Reference("GAR002", "Garantie2");
        Dependance dep1 = new Dependance(ref1);
        Dependance dep2 = new Dependance(ref2);

        assertThat(dep1.hashCode()).isNotEqualTo(dep2.hashCode());
    }

    @Test
    void shouldContainFieldValuesInToString() {
        Reference reference = new Reference("GAR003", "Label Garantie");
        Dependance dependance = new Dependance(reference);
        String toStringResult = dependance.toString();

        assertThat(toStringResult).contains("Dependance", "GAR003");
    }

    @Test
    void shouldHandleNullReference() {
        Dependance dependance = new Dependance(null);

        assertThat(dependance.garantieRequise()).isNull();
    }
}

