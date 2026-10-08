package fr.hm.tarificateur.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class QuoteTotalsTest {

    @Test
    void shouldCreateRecordWithValidValues() {
        Double monthly = 50.0;
        Double annual = 600.0;
        String currency = "EUR";

        QuoteTotals result = new QuoteTotals(monthly, annual, currency);

        assertThat(result).isNotNull();
        assertThat(result.monthly()).isEqualTo(50.0);
        assertThat(result.annual()).isEqualTo(600.0);
        assertThat(result.currency()).isEqualTo("EUR");
    }

    @Test
    void shouldReturnCorrectGetterValues() {
        QuoteTotals totals = new QuoteTotals(75.5, 906.0, "USD");

        assertThat(totals.monthly()).isEqualTo(75.5);
        assertThat(totals.annual()).isEqualTo(906.0);
        assertThat(totals.currency()).isEqualTo("USD");
    }

    @Test
    void shouldBeEqualWhenSameValues() {
        QuoteTotals totals1 = new QuoteTotals(50.0, 600.0, "EUR");
        QuoteTotals totals2 = new QuoteTotals(50.0, 600.0, "EUR");

        assertThat(totals1).isEqualTo(totals2);
    }

    @Test
    void shouldNotBeEqualWhenDifferentValues() {
        QuoteTotals totals1 = new QuoteTotals(50.0, 600.0, "EUR");
        QuoteTotals totals2 = new QuoteTotals(75.5, 906.0, "USD");

        assertThat(totals1).isNotEqualTo(totals2);
    }

    @Test
    void shouldHaveSameHashCodeForEqualObjects() {
        QuoteTotals totals1 = new QuoteTotals(50.0, 600.0, "EUR");
        QuoteTotals totals2 = new QuoteTotals(50.0, 600.0, "EUR");

        assertThat(totals1).hasSameHashCodeAs(totals2);
    }

    @Test
    void shouldHaveDifferentHashCodeForDifferentObjects() {
        QuoteTotals totals1 = new QuoteTotals(50.0, 600.0, "EUR");
        QuoteTotals totals2 = new QuoteTotals(75.5, 906.0, "USD");

        assertThat(totals1.hashCode()).isNotEqualTo(totals2.hashCode());
    }

    @Test
    void shouldContainFieldValuesInToString() {
        QuoteTotals totals = new QuoteTotals(100.25, 1203.0, "GBP");
        String toStringResult = totals.toString();

        assertThat(toStringResult).contains("QuoteTotals", "100.25", "1203.0", "GBP");
    }

    @Test
    void shouldHandleNullValues() {
        QuoteTotals totals = new QuoteTotals(null, null, null);

        assertThat(totals.monthly()).isNull();
        assertThat(totals.annual()).isNull();
        assertThat(totals.currency()).isNull();
    }

    @Test
    void shouldHandleZeroValues() {
        QuoteTotals totals = new QuoteTotals(0.0, 0.0, "EUR");

        assertThat(totals.monthly()).isEqualTo(0.0);
        assertThat(totals.annual()).isEqualTo(0.0);
        assertThat(totals.currency()).isEqualTo("EUR");
    }

    @Test
    void shouldHandleNegativeValues() {
        QuoteTotals totals = new QuoteTotals(-10.5, -126.0, "EUR");

        assertThat(totals.monthly()).isEqualTo(-10.5);
        assertThat(totals.annual()).isEqualTo(-126.0);
    }

    @Test
    void shouldHandleLargeValues() {
        QuoteTotals totals = new QuoteTotals(999999.99, 11999999.88, "EUR");

        assertThat(totals.monthly()).isEqualTo(999999.99);
        assertThat(totals.annual()).isEqualTo(11999999.88);
    }
}

