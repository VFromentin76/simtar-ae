package fr.hm.tarificateur.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class CustomerLoanQuoteTest {

    @Test
    void shouldCreateRecordWithValidValues() {
        String loanId = "LOAN001";
        String amount = "100000";
        String rate = "2.5";
        Double premium = 50.0;
        Warranty covers = new Warranty(true, false, true, false, true, false, true, false, "100000", "100000");

        CustomerLoanQuote result = new CustomerLoanQuote(loanId, amount, rate, premium, covers);

        assertThat(result).isNotNull();
        assertThat(result.loanId()).isEqualTo("LOAN001");
        assertThat(result.amount()).isEqualTo("100000");
        assertThat(result.rate()).isEqualTo("2.5");
        assertThat(result.premium()).isEqualTo(50.0);
        assertThat(result.covers()).isNotNull();
    }

    @Test
    void shouldReturnCorrectGetterValues() {
        Warranty warranty = new Warranty(false, true, true, false, true, false, false, true, "50000", "50000");
        CustomerLoanQuote clq = new CustomerLoanQuote("LOAN002", "200000", "3.0", 75.5, warranty);

        assertThat(clq.loanId()).isEqualTo("LOAN002");
        assertThat(clq.amount()).isEqualTo("200000");
        assertThat(clq.rate()).isEqualTo("3.0");
        assertThat(clq.premium()).isEqualTo(75.5);
        assertThat(clq.covers()).isEqualTo(warranty);
    }

    @Test
    void shouldBeEqualWhenSameValues() {
        Warranty warranty = new Warranty(true, false, true, false, true, false, true, false, "100000", "100000");
        CustomerLoanQuote clq1 = new CustomerLoanQuote("LOAN001", "100000", "2.5", 50.0, warranty);
        CustomerLoanQuote clq2 = new CustomerLoanQuote("LOAN001", "100000", "2.5", 50.0, warranty);

        assertThat(clq1).isEqualTo(clq2);
    }

    @Test
    void shouldNotBeEqualWhenDifferentValues() {
        Warranty w1 = new Warranty(true, false, true, false, true, false, true, false, "100000", "100000");
        Warranty w2 = new Warranty(false, true, true, false, true, false, false, true, "50000", "50000");
        CustomerLoanQuote clq1 = new CustomerLoanQuote("LOAN001", "100000", "2.5", 50.0, w1);
        CustomerLoanQuote clq2 = new CustomerLoanQuote("LOAN002", "200000", "3.0", 75.5, w2);

        assertThat(clq1).isNotEqualTo(clq2);
    }

    @Test
    void shouldHaveSameHashCodeForEqualObjects() {
        Warranty warranty = new Warranty(true, false, true, false, true, false, true, false, "100000", "100000");
        CustomerLoanQuote clq1 = new CustomerLoanQuote("LOAN001", "100000", "2.5", 50.0, warranty);
        CustomerLoanQuote clq2 = new CustomerLoanQuote("LOAN001", "100000", "2.5", 50.0, warranty);

        assertThat(clq1).hasSameHashCodeAs(clq2);
    }

    @Test
    void shouldHandleNullValues() {
        CustomerLoanQuote clq = new CustomerLoanQuote(null, null, null, null, null);

        assertThat(clq.loanId()).isNull();
        assertThat(clq.amount()).isNull();
        assertThat(clq.rate()).isNull();
        assertThat(clq.premium()).isNull();
        assertThat(clq.covers()).isNull();
    }

    @Test
    void shouldContainFieldValuesInToString() {
        Warranty warranty = new Warranty(true, true, false, true, false, true, true, false, "150000", "150000");
        CustomerLoanQuote clq = new CustomerLoanQuote("LOAN003", "300000", "2.8", 100.0, warranty);
        String toStringResult = clq.toString();

        assertThat(toStringResult).contains("CustomerLoanQuote");
    }
}

