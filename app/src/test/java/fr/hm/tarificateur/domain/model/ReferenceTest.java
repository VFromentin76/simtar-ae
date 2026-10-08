package fr.hm.tarificateur.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ReferenceTest {

    @Test
    void shouldCreateRecordWithValidValues() {
        String code = "REF001";
        String libelle = "Reference Name";

        Reference result = new Reference(code, libelle);

        assertThat(result).isNotNull();
        assertThat(result.code()).isEqualTo("REF001");
        assertThat(result.libelle()).isEqualTo("Reference Name");
    }

    @Test
    void shouldReturnCorrectGetterValues() {
        Reference reference = new Reference("PROD002", "Product Description");

        assertThat(reference.code()).isEqualTo("PROD002");
        assertThat(reference.libelle()).isEqualTo("Product Description");
    }

    @Test
    void shouldBeEqualWhenSameValues() {
        Reference ref1 = new Reference("REF001", "Reference Name");
        Reference ref2 = new Reference("REF001", "Reference Name");

        assertThat(ref1).isEqualTo(ref2);
    }

    @Test
    void shouldNotBeEqualWhenDifferentValues() {
        Reference ref1 = new Reference("REF001", "Reference Name");
        Reference ref2 = new Reference("REF002", "Other Name");

        assertThat(ref1).isNotEqualTo(ref2);
    }

    @Test
    void shouldHaveSameHashCodeForEqualObjects() {
        Reference ref1 = new Reference("REF001", "Reference Name");
        Reference ref2 = new Reference("REF001", "Reference Name");

        assertThat(ref1).hasSameHashCodeAs(ref2);
    }

    @Test
    void shouldHaveDifferentHashCodeForDifferentObjects() {
        Reference ref1 = new Reference("REF001", "Reference Name");
        Reference ref2 = new Reference("REF002", "Other Name");

        assertThat(ref1.hashCode()).isNotEqualTo(ref2.hashCode());
    }

    @Test
    void shouldContainFieldValuesInToString() {
        Reference reference = new Reference("CODE123", "Label 123");
        String toStringResult = reference.toString();

        assertThat(toStringResult).contains("Reference", "CODE123", "Label 123");
    }

    @Test
    void shouldHandleNullValues() {
        Reference reference = new Reference(null, null);

        assertThat(reference.code()).isNull();
        assertThat(reference.libelle()).isNull();
    }

    @Test
    void shouldHandleEmptyStrings() {
        Reference reference = new Reference("", "");

        assertThat(reference.code()).isEmpty();
        assertThat(reference.libelle()).isEmpty();
    }

    @Test
    void shouldHandleLongStrings() {
        String longCode = "A".repeat(1000);
        String longLibelle = "B".repeat(1000);
        Reference reference = new Reference(longCode, longLibelle);

        assertThat(reference.code()).isEqualTo(longCode);
        assertThat(reference.libelle()).isEqualTo(longLibelle);
    }

    @Test
    void shouldHandleSpecialCharacters() {
        Reference reference = new Reference("REF-001_v2", "Label @#$ & Special");

        assertThat(reference.code()).isEqualTo("REF-001_v2");
        assertThat(reference.libelle()).isEqualTo("Label @#$ & Special");
    }
}

