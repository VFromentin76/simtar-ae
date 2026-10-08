package fr.hm.tarificateur.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class CoefficientPassageFumeurCiTest {

    @Test
    void shouldCreateRecordWithValidValues() {
        Integer ageAdhesion = 40;
        String branche = "CI";
        Double coefficient = 1.2;

        CoefficientPassageFumeurCi result = new CoefficientPassageFumeurCi(ageAdhesion, branche, coefficient);

        assertThat(result).isNotNull();
        assertThat(result.ageAdhesion()).isEqualTo(40);
        assertThat(result.branche()).isEqualTo("CI");
        assertThat(result.coefficient()).isEqualTo(1.2);
    }

    @Test
    void shouldReturnCorrectGetterValues() {
        CoefficientPassageFumeurCi cpf = new CoefficientPassageFumeurCi(50, "CRD", 1.3);

        assertThat(cpf.ageAdhesion()).isEqualTo(50);
        assertThat(cpf.branche()).isEqualTo("CRD");
        assertThat(cpf.coefficient()).isEqualTo(1.3);
    }

    @Test
    void shouldBeEqualWhenSameValues() {
        CoefficientPassageFumeurCi cpf1 = new CoefficientPassageFumeurCi(40, "CI", 1.2);
        CoefficientPassageFumeurCi cpf2 = new CoefficientPassageFumeurCi(40, "CI", 1.2);

        assertThat(cpf1).isEqualTo(cpf2);
    }

    @Test
    void shouldNotBeEqualWhenDifferentValues() {
        CoefficientPassageFumeurCi cpf1 = new CoefficientPassageFumeurCi(40, "CI", 1.2);
        CoefficientPassageFumeurCi cpf2 = new CoefficientPassageFumeurCi(50, "CRD", 1.3);

        assertThat(cpf1).isNotEqualTo(cpf2);
    }

    @Test
    void shouldHaveSameHashCodeForEqualObjects() {
        CoefficientPassageFumeurCi cpf1 = new CoefficientPassageFumeurCi(40, "CI", 1.2);
        CoefficientPassageFumeurCi cpf2 = new CoefficientPassageFumeurCi(40, "CI", 1.2);

        assertThat(cpf1).hasSameHashCodeAs(cpf2);
    }

    @Test
    void shouldContainFieldValuesInToString() {
        CoefficientPassageFumeurCi cpf = new CoefficientPassageFumeurCi(45, "CI", 1.25);
        String toStringResult = cpf.toString();

        assertThat(toStringResult).contains("CoefficientPassageFumeurCi");
    }

    @Test
    void shouldHandleNullValues() {
        CoefficientPassageFumeurCi cpf = new CoefficientPassageFumeurCi(null, null, null);

        assertThat(cpf.ageAdhesion()).isNull();
        assertThat(cpf.branche()).isNull();
        assertThat(cpf.coefficient()).isNull();
    }

    @Test
    void shouldHandleEmptyBranche() {
        CoefficientPassageFumeurCi cpf = new CoefficientPassageFumeurCi(40, "", 1.2);

        assertThat(cpf.branche()).isEmpty();
    }

    @Test
    void shouldHandleZeroAge() {
        CoefficientPassageFumeurCi cpf = new CoefficientPassageFumeurCi(0, "CI", 1.2);

        assertThat(cpf.ageAdhesion()).isEqualTo(0);
    }

    @Test
    void shouldHandleLargeValues() {
        CoefficientPassageFumeurCi cpf = new CoefficientPassageFumeurCi(150, "BRANCHE", 999.99);

        assertThat(cpf.ageAdhesion()).isEqualTo(150);
        assertThat(cpf.coefficient()).isEqualTo(999.99);
    }
}

