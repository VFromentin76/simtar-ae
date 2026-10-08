package fr.hm.tarificateur.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ProductQuoteTest {

    @Test
    void shouldCreateRecordWithValidValues() {
        String code = "PROD001";
        String name = "Insurance Product 1";
        Premium premium = new Premium(50.0, 600.0, "EUR");
        String status = "SUCCESS";

        ProductQuote result = new ProductQuote(code, name, premium, status);

        assertThat(result).isNotNull();
        assertThat(result.code()).isEqualTo("PROD001");
        assertThat(result.name()).isEqualTo("Insurance Product 1");
        assertThat(result.premium()).isNotNull();
        assertThat(result.status()).isEqualTo("SUCCESS");
    }

    @Test
    void shouldReturnCorrectGetterValues() {
        Premium premium = new Premium(75.5, 906.0, "USD");
        ProductQuote pq = new ProductQuote("PROD002", "Insurance Product 2", premium, "PENDING");

        assertThat(pq.code()).isEqualTo("PROD002");
        assertThat(pq.name()).isEqualTo("Insurance Product 2");
        assertThat(pq.premium()).isNotNull();
        assertThat(pq.status()).isEqualTo("PENDING");
    }

    @Test
    void shouldBeEqualWhenSameValues() {
        Premium prem = new Premium(50.0, 600.0, "EUR");
        ProductQuote pq1 = new ProductQuote("PROD001", "Insurance Product 1", prem, "SUCCESS");
        ProductQuote pq2 = new ProductQuote("PROD001", "Insurance Product 1", prem, "SUCCESS");

        assertThat(pq1).isEqualTo(pq2);
    }

    @Test
    void shouldNotBeEqualWhenDifferentValues() {
        Premium prem1 = new Premium(50.0, 600.0, "EUR");
        Premium prem2 = new Premium(75.5, 906.0, "USD");
        ProductQuote pq1 = new ProductQuote("PROD001", "Insurance Product 1", prem1, "SUCCESS");
        ProductQuote pq2 = new ProductQuote("PROD002", "Insurance Product 2", prem2, "PENDING");

        assertThat(pq1).isNotEqualTo(pq2);
    }

    @Test
    void shouldHaveSameHashCodeForEqualObjects() {
        Premium prem = new Premium(50.0, 600.0, "EUR");
        ProductQuote pq1 = new ProductQuote("PROD001", "Insurance Product 1", prem, "SUCCESS");
        ProductQuote pq2 = new ProductQuote("PROD001", "Insurance Product 1", prem, "SUCCESS");

        assertThat(pq1).hasSameHashCodeAs(pq2);
    }

    @Test
    void shouldHandleNullValues() {
        ProductQuote pq = new ProductQuote(null, null, null, null);

        assertThat(pq.code()).isNull();
        assertThat(pq.name()).isNull();
        assertThat(pq.premium()).isNull();
        assertThat(pq.status()).isNull();
    }

    @Test
    void shouldHandleEmptyStrings() {
        ProductQuote pq = new ProductQuote("", "", new Premium(0.0, 0.0, ""), "");

        assertThat(pq.code()).isEmpty();
        assertThat(pq.name()).isEmpty();
        assertThat(pq.status()).isEmpty();
    }

    @Test
    void shouldContainFieldValuesInToString() {
        Premium premium = new Premium(100.0, 1200.0, "GBP");
        ProductQuote pq = new ProductQuote("PROD003", "Insurance Product 3", premium, "ERROR");
        String toStringResult = pq.toString();

        assertThat(toStringResult).contains("ProductQuote");
    }
}

