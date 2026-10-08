package fr.hm.tarificateur.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class CustomerQuoteTest {

    @Test
    void shouldCreateRecordWithValidValues() {
        String customerRef = "CUST001";
        String status = "SUCCESS";
        Premium premium = new Premium(50.0, 600.0, "EUR");

        CustomerQuote result = new CustomerQuote(customerRef, status, premium, null, null);

        assertThat(result).isNotNull();
        assertThat(result.customerRef()).isEqualTo("CUST001");
        assertThat(result.status()).isEqualTo("SUCCESS");
        assertThat(result.premium()).isEqualTo(premium);
        assertThat(result.loans()).isNull();
        assertThat(result.warnings()).isNull();
    }

    @Test
    void shouldReturnCorrectGetterValues() {
        Premium premium = new Premium(75.5, 906.0, "USD");
        CustomerQuote cq = new CustomerQuote("CUST002", "PENDING", premium, null, null);

        assertThat(cq.customerRef()).isEqualTo("CUST002");
        assertThat(cq.status()).isEqualTo("PENDING");
        assertThat(cq.premium()).isNotNull();
    }

    @Test
    void shouldBeEqualWhenSameValues() {
        Premium prem = new Premium(50.0, 600.0, "EUR");
        CustomerQuote cq1 = new CustomerQuote("CUST001", "SUCCESS", prem, null, null);
        CustomerQuote cq2 = new CustomerQuote("CUST001", "SUCCESS", prem, null, null);

        assertThat(cq1).isEqualTo(cq2);
    }

    @Test
    void shouldNotBeEqualWhenDifferentValues() {
        Premium prem1 = new Premium(50.0, 600.0, "EUR");
        Premium prem2 = new Premium(75.5, 906.0, "USD");
        CustomerQuote cq1 = new CustomerQuote("CUST001", "SUCCESS", prem1, null, null);
        CustomerQuote cq2 = new CustomerQuote("CUST002", "PENDING", prem2, null, null);

        assertThat(cq1).isNotEqualTo(cq2);
    }

    @Test
    void shouldHaveSameHashCodeForEqualObjects() {
        Premium prem = new Premium(50.0, 600.0, "EUR");
        CustomerQuote cq1 = new CustomerQuote("CUST001", "SUCCESS", prem, null, null);
        CustomerQuote cq2 = new CustomerQuote("CUST001", "SUCCESS", prem, null, null);

        assertThat(cq1).hasSameHashCodeAs(cq2);
    }

    @Test
    void shouldHandleNullValues() {
        CustomerQuote cq = new CustomerQuote(null, null, null, null, null);

        assertThat(cq.customerRef()).isNull();
        assertThat(cq.status()).isNull();
        assertThat(cq.premium()).isNull();
        assertThat(cq.loans()).isNull();
        assertThat(cq.warnings()).isNull();
    }

    @Test
    void shouldContainFieldValuesInToString() {
        Premium premium = new Premium(100.0, 1200.0, "GBP");
        CustomerQuote cq = new CustomerQuote("CUST003", "ERROR", premium, null, null);
        String toStringResult = cq.toString();

        assertThat(toStringResult).contains("CustomerQuote");
    }
}

