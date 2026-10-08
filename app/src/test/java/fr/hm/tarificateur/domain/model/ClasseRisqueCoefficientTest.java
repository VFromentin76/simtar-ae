package fr.hm.tarificateur.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ClasseRisqueCoefficientTest {

    @Test
    void shouldCreateRecordWithValidValues() {
        Reference classeRisque = new Reference("CR001", "Classe Risque 1");
        String branche = "CI";
        Boolean regimeLemoine = true;
        Double coeffPassage = 0.9;

        ClasseRisqueCoefficient result = new ClasseRisqueCoefficient(classeRisque, branche, regimeLemoine, coeffPassage);

        assertThat(result).isNotNull();
        assertThat(result.classeRisque()).isEqualTo(classeRisque);
        assertThat(result.branche()).isEqualTo("CI");
        assertThat(result.regimeLemoine()).isTrue();
        assertThat(result.coeffPassage()).isEqualTo(0.9);
    }

    @Test
    void shouldReturnCorrectGetterValues() {
        Reference cr = new Reference("CR002", "Classe Risque 2");
        ClasseRisqueCoefficient crc = new ClasseRisqueCoefficient(cr, "CRD", false, 1.05);

        assertThat(crc.classeRisque()).isEqualTo(cr);
        assertThat(crc.branche()).isEqualTo("CRD");
        assertThat(crc.regimeLemoine()).isFalse();
        assertThat(crc.coeffPassage()).isEqualTo(1.05);
    }

    @Test
    void shouldBeEqualWhenSameValues() {
        Reference ref = new Reference("CR001", "Classe Risque 1");
        ClasseRisqueCoefficient crc1 = new ClasseRisqueCoefficient(ref, "CI", true, 0.9);
        ClasseRisqueCoefficient crc2 = new ClasseRisqueCoefficient(ref, "CI", true, 0.9);

        assertThat(crc1).isEqualTo(crc2);
    }

    @Test
    void shouldNotBeEqualWhenDifferentValues() {
        Reference ref1 = new Reference("CR001", "Classe Risque 1");
        Reference ref2 = new Reference("CR002", "Classe Risque 2");
        ClasseRisqueCoefficient crc1 = new ClasseRisqueCoefficient(ref1, "CI", true, 0.9);
        ClasseRisqueCoefficient crc2 = new ClasseRisqueCoefficient(ref2, "CRD", false, 1.05);

        assertThat(crc1).isNotEqualTo(crc2);
    }

    @Test
    void shouldHaveSameHashCodeForEqualObjects() {
        Reference ref = new Reference("CR001", "Classe Risque 1");
        ClasseRisqueCoefficient crc1 = new ClasseRisqueCoefficient(ref, "CI", true, 0.9);
        ClasseRisqueCoefficient crc2 = new ClasseRisqueCoefficient(ref, "CI", true, 0.9);

        assertThat(crc1).hasSameHashCodeAs(crc2);
    }

    @Test
    void shouldContainFieldValuesInToString() {
        Reference ref = new Reference("CR001", "Classe Risque 1");
        ClasseRisqueCoefficient crc = new ClasseRisqueCoefficient(ref, "CI", true, 0.9);
        String toStringResult = crc.toString();

        assertThat(toStringResult).contains("ClasseRisqueCoefficient");
    }

    @Test
    void shouldHandleNullValues() {
        ClasseRisqueCoefficient crc = new ClasseRisqueCoefficient(null, null, null, null);

        assertThat(crc.classeRisque()).isNull();
        assertThat(crc.branche()).isNull();
        assertThat(crc.regimeLemoine()).isNull();
        assertThat(crc.coeffPassage()).isNull();
    }

    @Test
    void shouldHandleEmptyBranche() {
        Reference ref = new Reference("CR001", "Classe Risque 1");
        ClasseRisqueCoefficient crc = new ClasseRisqueCoefficient(ref, "", true, 0.9);

        assertThat(crc.branche()).isEmpty();
    }
}

