package fr.hm.tarificateur.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class FranchiseCoefficientTest {

    @Test
    void shouldCreateRecordWithValidValues() {
        Reference franchise = new Reference("FRAN001", "Franchise 1000");
        Boolean regimeLemoine = true;
        Double coefficient = 0.95;

        FranchiseCoefficient result = new FranchiseCoefficient(franchise, regimeLemoine, coefficient);

        assertThat(result).isNotNull();
        assertThat(result.franchise()).isEqualTo(franchise);
        assertThat(result.regimeLemoine()).isTrue();
        assertThat(result.coefficient()).isEqualTo(0.95);
    }

    @Test
    void shouldReturnCorrectGetterValues() {
        Reference franchise = new Reference("FRAN002", "Franchise 2000");
        FranchiseCoefficient fc = new FranchiseCoefficient(franchise, false, 1.05);

        assertThat(fc.franchise()).isEqualTo(franchise);
        assertThat(fc.regimeLemoine()).isFalse();
        assertThat(fc.coefficient()).isEqualTo(1.05);
    }

    @Test
    void shouldBeEqualWhenSameValues() {
        Reference ref = new Reference("FRAN001", "Franchise 1000");
        FranchiseCoefficient fc1 = new FranchiseCoefficient(ref, true, 0.95);
        FranchiseCoefficient fc2 = new FranchiseCoefficient(ref, true, 0.95);

        assertThat(fc1).isEqualTo(fc2);
    }

    @Test
    void shouldNotBeEqualWhenDifferentValues() {
        Reference ref1 = new Reference("FRAN001", "Franchise 1000");
        Reference ref2 = new Reference("FRAN002", "Franchise 2000");
        FranchiseCoefficient fc1 = new FranchiseCoefficient(ref1, true, 0.95);
        FranchiseCoefficient fc2 = new FranchiseCoefficient(ref2, false, 1.05);

        assertThat(fc1).isNotEqualTo(fc2);
    }

    @Test
    void shouldHaveSameHashCodeForEqualObjects() {
        Reference ref = new Reference("FRAN001", "Franchise 1000");
        FranchiseCoefficient fc1 = new FranchiseCoefficient(ref, true, 0.95);
        FranchiseCoefficient fc2 = new FranchiseCoefficient(ref, true, 0.95);

        assertThat(fc1).hasSameHashCodeAs(fc2);
    }

    @Test
    void shouldContainFieldValuesInToString() {
        Reference ref = new Reference("FRAN001", "Franchise 1000");
        FranchiseCoefficient fc = new FranchiseCoefficient(ref, true, 0.95);
        String toStringResult = fc.toString();

        assertThat(toStringResult).contains("FranchiseCoefficient");
    }

    @Test
    void shouldHandleNullReference() {
        FranchiseCoefficient fc = new FranchiseCoefficient(null, true, 0.95);

        assertThat(fc.franchise()).isNull();
        assertThat(fc.regimeLemoine()).isTrue();
        assertThat(fc.coefficient()).isEqualTo(0.95);
    }

    @Test
    void shouldHandleNullCoefficient() {
        Reference ref = new Reference("FRAN001", "Franchise 1000");
        FranchiseCoefficient fc = new FranchiseCoefficient(ref, true, null);

        assertThat(fc.coefficient()).isNull();
    }

    @Test
    void shouldHandleNullRegimeLemoine() {
        Reference ref = new Reference("FRAN001", "Franchise 1000");
        FranchiseCoefficient fc = new FranchiseCoefficient(ref, null, 0.95);

        assertThat(fc.regimeLemoine()).isNull();
    }

    @Test
    void shouldHandleVariousCoefficientValues() {
        Reference ref = new Reference("FRAN001", "Franchise 1000");
        FranchiseCoefficient fc1 = new FranchiseCoefficient(ref, true, 0.0);
        FranchiseCoefficient fc2 = new FranchiseCoefficient(ref, false, 10.5);

        assertThat(fc1.coefficient()).isEqualTo(0.0);
        assertThat(fc2.coefficient()).isEqualTo(10.5);
    }
}

