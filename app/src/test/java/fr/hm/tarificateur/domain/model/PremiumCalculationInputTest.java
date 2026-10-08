package fr.hm.tarificateur.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class PremiumCalculationInputTest {

    @Test
    void shouldCreateRecordWithValidValues() {
        Double loanAmount = 100000.0;
        Integer ageAdhesion = 35;
        Integer loanDurationYears = 15;
        String calculationMode = "STANDARD";

        PremiumCalculationInput result = new PremiumCalculationInput(loanAmount, ageAdhesion, loanDurationYears, calculationMode);

        assertThat(result).isNotNull();
        assertThat(result.loanAmount()).isEqualTo(100000.0);
        assertThat(result.ageAdhesion()).isEqualTo(35);
        assertThat(result.loanDurationYears()).isEqualTo(15);
        assertThat(result.calculationMode()).isEqualTo("STANDARD");
    }

    @Test
    void shouldReturnCorrectGetterValues() {
        PremiumCalculationInput pci = new PremiumCalculationInput(250000.0, 45, 20, "ADVANCED");

        assertThat(pci.loanAmount()).isEqualTo(250000.0);
        assertThat(pci.ageAdhesion()).isEqualTo(45);
        assertThat(pci.loanDurationYears()).isEqualTo(20);
        assertThat(pci.calculationMode()).isEqualTo("ADVANCED");
    }

    @Test
    void shouldBeEqualWhenSameValues() {
        PremiumCalculationInput pci1 = new PremiumCalculationInput(100000.0, 35, 15, "STANDARD");
        PremiumCalculationInput pci2 = new PremiumCalculationInput(100000.0, 35, 15, "STANDARD");

        assertThat(pci1).isEqualTo(pci2);
    }

    @Test
    void shouldNotBeEqualWhenDifferentValues() {
        PremiumCalculationInput pci1 = new PremiumCalculationInput(100000.0, 35, 15, "STANDARD");
        PremiumCalculationInput pci2 = new PremiumCalculationInput(250000.0, 45, 20, "ADVANCED");

        assertThat(pci1).isNotEqualTo(pci2);
    }

    @Test
    void shouldHaveSameHashCodeForEqualObjects() {
        PremiumCalculationInput pci1 = new PremiumCalculationInput(100000.0, 35, 15, "STANDARD");
        PremiumCalculationInput pci2 = new PremiumCalculationInput(100000.0, 35, 15, "STANDARD");

        assertThat(pci1).hasSameHashCodeAs(pci2);
    }

    @Test
    void shouldContainFieldValuesInToString() {
        PremiumCalculationInput pci = new PremiumCalculationInput(150000.0, 40, 18, "CUSTOM");
        String toStringResult = pci.toString();

        assertThat(toStringResult).contains("PremiumCalculationInput");
    }

    @Test
    void shouldHandleNullValues() {
        PremiumCalculationInput pci = new PremiumCalculationInput(null, null, null, null);

        assertThat(pci.loanAmount()).isNull();
        assertThat(pci.ageAdhesion()).isNull();
        assertThat(pci.loanDurationYears()).isNull();
        assertThat(pci.calculationMode()).isNull();
    }

    @Test
    void shouldHandleZeroValues() {
        PremiumCalculationInput pci = new PremiumCalculationInput(0.0, 0, 0, "STANDARD");

        assertThat(pci.loanAmount()).isEqualTo(0.0);
        assertThat(pci.ageAdhesion()).isEqualTo(0);
        assertThat(pci.loanDurationYears()).isEqualTo(0);
    }

    @Test
    void shouldHandleLargeValues() {
        PremiumCalculationInput pci = new PremiumCalculationInput(9999999.99, 120, 500, "EXTREME");

        assertThat(pci.loanAmount()).isEqualTo(9999999.99);
        assertThat(pci.ageAdhesion()).isEqualTo(120);
        assertThat(pci.loanDurationYears()).isEqualTo(500);
    }

    @Test
    void shouldHandleEmptyCalculationMode() {
        PremiumCalculationInput pci = new PremiumCalculationInput(100000.0, 35, 15, "");

        assertThat(pci.calculationMode()).isEmpty();
    }
}

