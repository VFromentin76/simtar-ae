package fr.hm.tarificateur.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class PremiumTest {

    @Test
    void shouldCreateRecordWithValidValues() {
        Double monthly = 45.0;
        Double annual = 540.0;
        String currency = "EUR";

        Premium result = new Premium(monthly, annual, currency);

        assertThat(result).isNotNull();
        assertThat(result.monthly()).isEqualTo(45.0);
        assertThat(result.annual()).isEqualTo(540.0);
        assertThat(result.currency()).isEqualTo("EUR");
    }

    @Test
    void shouldReturnCorrectGetterValues() {
        Premium premium = new Premium(65.5, 786.0, "USD");

        assertThat(premium.monthly()).isEqualTo(65.5);
        assertThat(premium.annual()).isEqualTo(786.0);
        assertThat(premium.currency()).isEqualTo("USD");
    }

    @Test
    void shouldBeEqualWhenSameValues() {
        Premium prem1 = new Premium(45.0, 540.0, "EUR");
        Premium prem2 = new Premium(45.0, 540.0, "EUR");

        assertThat(prem1).isEqualTo(prem2);
    }

    @Test
    void shouldNotBeEqualWhenDifferentValues() {
        Premium prem1 = new Premium(45.0, 540.0, "EUR");
        Premium prem2 = new Premium(65.5, 786.0, "USD");

        assertThat(prem1).isNotEqualTo(prem2);
    }

    @Test
    void shouldHaveSameHashCodeForEqualObjects() {
        Premium prem1 = new Premium(45.0, 540.0, "EUR");
        Premium prem2 = new Premium(45.0, 540.0, "EUR");

        assertThat(prem1).hasSameHashCodeAs(prem2);
    }

    @Test
    void shouldHaveDifferentHashCodeForDifferentObjects() {
        Premium prem1 = new Premium(45.0, 540.0, "EUR");
        Premium prem2 = new Premium(65.5, 786.0, "USD");

        assertThat(prem1.hashCode()).isNotEqualTo(prem2.hashCode());
    }

    @Test
    void shouldContainFieldValuesInToString() {
        Premium premium = new Premium(120.75, 1449.0, "CHF");
        String toStringResult = premium.toString();

        assertThat(toStringResult).contains("Premium", "120.75", "1449.0", "CHF");
    }

    @Test
    void shouldHandleNullValues() {
        Premium premium = new Premium(null, null, null);

        assertThat(premium.monthly()).isNull();
        assertThat(premium.annual()).isNull();
        assertThat(premium.currency()).isNull();
    }

    @Test
    void shouldHandleZeroValues() {
        Premium premium = new Premium(0.0, 0.0, "EUR");

        assertThat(premium.monthly()).isEqualTo(0.0);
        assertThat(premium.annual()).isEqualTo(0.0);
    }

    @Test
    void shouldHandleNegativeValues() {
        Premium premium = new Premium(-5.0, -60.0, "EUR");

        assertThat(premium.monthly()).isEqualTo(-5.0);
        assertThat(premium.annual()).isEqualTo(-60.0);
    }

    @Test
    void shouldHandleLargeValues() {
        Premium premium = new Premium(9999.99, 119999.88, "EUR");

        assertThat(premium.monthly()).isEqualTo(9999.99);
        assertThat(premium.annual()).isEqualTo(119999.88);
    }

    @Test
    void shouldHandleEmptyCurrency() {
        Premium premium = new Premium(50.0, 600.0, "");

        assertThat(premium.currency()).isEmpty();
    }
}

