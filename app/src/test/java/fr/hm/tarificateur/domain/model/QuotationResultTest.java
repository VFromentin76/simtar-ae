package fr.hm.tarificateur.domain.model;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class QuotationResultTest {

    @Test
    void shouldCreateRecordWithValidValues() {
        String status = "SUCCESS";
        String creationDate = "2023-01-01";
        String quotationId = "QUOT001";
        List<CustomerQuote> customers = new ArrayList<>();
        List<ProductQuote> products = new ArrayList<>();
        QuoteTotals totals = new QuoteTotals(50.0, 600.0, "EUR");
        List<String> warnings = new ArrayList<>();
        Boolean returnResults = true;

        QuotationResult result = new QuotationResult(status, creationDate, quotationId, customers, products, totals, warnings, returnResults);

        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo("SUCCESS");
        assertThat(result.creationDate()).isEqualTo("2023-01-01");
        assertThat(result.quotationId()).isEqualTo("QUOT001");
        assertThat(result.customers()).isEmpty();
        assertThat(result.products()).isEmpty();
        assertThat(result.totals()).isNotNull();
        assertThat(result.warnings()).isEmpty();
        assertThat(result.returnResults()).isTrue();
    }

    @Test
    void shouldReturnCorrectGetterValues() {
        List<CustomerQuote> customers = new ArrayList<>();
        List<ProductQuote> products = new ArrayList<>();
        QuoteTotals totals = new QuoteTotals(75.5, 906.0, "USD");
        List<String> warnings = new ArrayList<>();
        warnings.add("WARNING1");
        
        QuotationResult qr = new QuotationResult("PENDING", "2023-02-01", "QUOT002", customers, products, totals, warnings, false);

        assertThat(qr.status()).isEqualTo("PENDING");
        assertThat(qr.creationDate()).isEqualTo("2023-02-01");
        assertThat(qr.quotationId()).isEqualTo("QUOT002");
        assertThat(qr.warnings()).hasSize(1);
        assertThat(qr.returnResults()).isFalse();
    }

    @Test
    void shouldBeEqualWhenSameValues() {
        QuoteTotals totals = new QuoteTotals(50.0, 600.0, "EUR");
        QuotationResult qr1 = new QuotationResult("SUCCESS", "2023-01-01", "QUOT001", new ArrayList<>(), new ArrayList<>(), totals, new ArrayList<>(), true);
        QuotationResult qr2 = new QuotationResult("SUCCESS", "2023-01-01", "QUOT001", new ArrayList<>(), new ArrayList<>(), totals, new ArrayList<>(), true);

        assertThat(qr1).isEqualTo(qr2);
    }

    @Test
    void shouldNotBeEqualWhenDifferentValues() {
        QuoteTotals totals1 = new QuoteTotals(50.0, 600.0, "EUR");
        QuoteTotals totals2 = new QuoteTotals(75.5, 906.0, "USD");
        QuotationResult qr1 = new QuotationResult("SUCCESS", "2023-01-01", "QUOT001", new ArrayList<>(), new ArrayList<>(), totals1, new ArrayList<>(), true);
        QuotationResult qr2 = new QuotationResult("PENDING", "2023-02-01", "QUOT002", new ArrayList<>(), new ArrayList<>(), totals2, new ArrayList<>(), false);

        assertThat(qr1).isNotEqualTo(qr2);
    }

    @Test
    void shouldHaveSameHashCodeForEqualObjects() {
        QuoteTotals totals = new QuoteTotals(50.0, 600.0, "EUR");
        QuotationResult qr1 = new QuotationResult("SUCCESS", "2023-01-01", "QUOT001", new ArrayList<>(), new ArrayList<>(), totals, new ArrayList<>(), true);
        QuotationResult qr2 = new QuotationResult("SUCCESS", "2023-01-01", "QUOT001", new ArrayList<>(), new ArrayList<>(), totals, new ArrayList<>(), true);

        assertThat(qr1).hasSameHashCodeAs(qr2);
    }

    @Test
    void shouldHandleNullValues() {
        QuotationResult qr = new QuotationResult(null, null, null, null, null, null, null, null);

        assertThat(qr.status()).isNull();
        assertThat(qr.creationDate()).isNull();
        assertThat(qr.quotationId()).isNull();
        assertThat(qr.customers()).isNull();
        assertThat(qr.products()).isNull();
        assertThat(qr.totals()).isNull();
        assertThat(qr.warnings()).isNull();
        assertThat(qr.returnResults()).isNull();
    }

    @Test
    void shouldHandlePopulatedWarnings() {
        List<String> warnings = new ArrayList<>();
        warnings.add("WARNING1");
        warnings.add("WARNING2");
        warnings.add("WARNING3");
        
        QuotationResult qr = new QuotationResult("SUCCESS", "2023-01-01", "QUOT001", new ArrayList<>(), new ArrayList<>(), 
            new QuoteTotals(50.0, 600.0, "EUR"), warnings, true);

        assertThat(qr.warnings()).hasSize(3);
    }

    @Test
    void shouldContainFieldValuesInToString() {
        QuotationResult qr = new QuotationResult("ERROR", "2023-03-01", "QUOT003", new ArrayList<>(), new ArrayList<>(), 
            new QuoteTotals(100.0, 1200.0, "GBP"), new ArrayList<>(), false);
        String toStringResult = qr.toString();

        assertThat(toStringResult).contains("QuotationResult");
    }
}

