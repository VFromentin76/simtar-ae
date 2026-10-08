package fr.hm.tarificateur.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CoverageEndCoefficientTest {

    @Test
    void shouldExposeCoverageEndRuleValues() {
        CoverageEndCoefficient rule = new CoverageEndCoefficient(35, 65, 1.0);

        assertThat(rule.ageAdhesion()).isEqualTo(35);
        assertThat(rule.ageFinCouverture()).isEqualTo(65);
        assertThat(rule.coefficient()).isEqualTo(1.0);
    }

    @Test
    void shouldCompareCoverageEndRulesByValue() {
        CoverageEndCoefficient first = new CoverageEndCoefficient(35, 65, 1.0);
        CoverageEndCoefficient second = new CoverageEndCoefficient(35, 65, 1.0);

        assertThat(first).isEqualTo(second).hasSameHashCodeAs(second);
        assertThat(first.toString()).contains("35", "65", "1.0");
    }
}
