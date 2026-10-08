package fr.hm.tarificateur.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class CoefficientPassageFumeurCrdTest {

    @Test
    void shouldCreateRecordWithValidValues() {
        Integer ageAtteint = 45;
        String branche = "CI";
        Double coefficient = 1.15;

        CoefficientPassageFumeurCrd result = new CoefficientPassageFumeurCrd(ageAtteint, branche, coefficient);

        assertThat(result).isNotNull();
        assertThat(result.ageAtteint()).isEqualTo(45);
        assertThat(result.branche()).isEqualTo("CI");
        assertThat(result.coefficient()).isEqualTo(1.15);
    }

    @Test
    void shouldReturnCorrectGetterValues() {
        CoefficientPassageFumeurCrd cpf = new CoefficientPassageFumeurCrd(50, "CRD", 1.25);

        assertThat(cpf.ageAtteint()).isEqualTo(50);
        assertThat(cpf.branche()).isEqualTo("CRD");
        assertThat(cpf.coefficient()).isEqualTo(1.25);
    }

    @Test
    void shouldBeEqualWhenSameValues() {
        CoefficientPassageFumeurCrd cpf1 = new CoefficientPassageFumeurCrd(45, "CI", 1.15);
        CoefficientPassageFumeurCrd cpf2 = new CoefficientPassageFumeurCrd(45, "CI", 1.15);

        assertThat(cpf1).isEqualTo(cpf2);
    }

    @Test
    void shouldNotBeEqualWhenDifferentValues() {
        CoefficientPassageFumeurCrd cpf1 = new CoefficientPassageFumeurCrd(45, "CI", 1.15);
        CoefficientPassageFumeurCrd cpf2 = new CoefficientPassageFumeurCrd(50, "CRD", 1.25);

        assertThat(cpf1).isNotEqualTo(cpf2);
    }

    @Test
    void shouldHaveSameHashCodeForEqualObjects() {
        CoefficientPassageFumeurCrd cpf1 = new CoefficientPassageFumeurCrd(45, "CI", 1.15);
        CoefficientPassageFumeurCrd cpf2 = new CoefficientPassageFumeurCrd(45, "CI", 1.15);

        assertThat(cpf1).hasSameHashCodeAs(cpf2);
    }

    @Test
    void shouldContainFieldValuesInToString() {
        CoefficientPassageFumeurCrd cpf = new CoefficientPassageFumeurCrd(55, "CRD", 1.3);
        String toStringResult = cpf.toString();

        assertThat(toStringResult).contains("CoefficientPassageFumeurCrd");
    }

    @Test
    void shouldHandleNullValues() {
        CoefficientPassageFumeurCrd cpf = new CoefficientPassageFumeurCrd(null, null, null);

        assertThat(cpf.ageAtteint()).isNull();
        assertThat(cpf.branche()).isNull();
        assertThat(cpf.coefficient()).isNull();
    }

    @Test
    void shouldHandleEmptyBranche() {
        CoefficientPassageFumeurCrd cpf = new CoefficientPassageFumeurCrd(45, "", 1.15);

        assertThat(cpf.branche()).isEmpty();
    }

    @Test
    void shouldHandleZeroAge() {
        CoefficientPassageFumeurCrd cpf = new CoefficientPassageFumeurCrd(0, "CI", 1.15);

        assertThat(cpf.ageAtteint()).isEqualTo(0);
    }
}

