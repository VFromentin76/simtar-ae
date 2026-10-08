package fr.hm.tarificateur.domain.model;

import org.junit.jupiter.api.Test;
import java.util.Map;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.*;

class QuotationTest {

    @Test
    void shouldCreateRecordWithValidValues() {
        String creationDate = "2023-01-01";
        Map<String, Customer> customers = new HashMap<>();
        QuotationOptions options = new QuotationOptions("BANK001", true, "2023-01-01", new HashMap<>(), 1, 2);
        String returnResults = "true";
        String source = "WEB";
        Integer paymentFrequency = 12;

        Quotation result = new Quotation(creationDate, customers, options, returnResults, source, paymentFrequency);

        assertThat(result).isNotNull();
        assertThat(result.creationDate()).isEqualTo("2023-01-01");
        assertThat(result.customers()).isEmpty();
        assertThat(result.options()).isNotNull();
        assertThat(result.returnResults()).isEqualTo("true");
        assertThat(result.source()).isEqualTo("WEB");
        assertThat(result.paymentFrequency()).isEqualTo(12);
    }

    @Test
    void shouldReturnCorrectGetterValues() {
        Map<String, Customer> customers = new HashMap<>();
        QuotationOptions options = new QuotationOptions("BANK002", false, "2023-02-01", new HashMap<>(), 2, 3);
        
        Quotation quotation = new Quotation("2023-02-01", customers, options, "false", "API", 24);

        assertThat(quotation.creationDate()).isEqualTo("2023-02-01");
        assertThat(quotation.source()).isEqualTo("API");
        assertThat(quotation.paymentFrequency()).isEqualTo(24);
    }

    @Test
    void shouldBeEqualWhenSameValues() {
        Map<String, Customer> customers1 = new HashMap<>();
        Map<String, Customer> customers2 = new HashMap<>();
        QuotationOptions options1 = new QuotationOptions("BANK001", true, "2023-01-01", new HashMap<>(), 1, 2);
        QuotationOptions options2 = new QuotationOptions("BANK001", true, "2023-01-01", new HashMap<>(), 1, 2);
        Quotation quot1 = new Quotation("2023-01-01", customers1, options1, "true", "WEB", 12);
        Quotation quot2 = new Quotation("2023-01-01", customers2, options2, "true", "WEB", 12);

        assertThat(quot1).isEqualTo(quot2);
    }

    @Test
    void shouldNotBeEqualWhenDifferentValues() {
        Quotation quot1 = new Quotation("2023-01-01", new HashMap<>(), 
            new QuotationOptions("BANK001", true, "2023-01-01", new HashMap<>(), 1, 2), "true", "WEB", 12);
        Quotation quot2 = new Quotation("2023-02-01", new HashMap<>(), 
            new QuotationOptions("BANK002", false, "2023-02-01", new HashMap<>(), 2, 3), "false", "API", 24);

        assertThat(quot1).isNotEqualTo(quot2);
    }

    @Test
    void shouldHaveSameHashCodeForEqualObjects() {
        QuotationOptions opts = new QuotationOptions("BANK001", true, "2023-01-01", new HashMap<>(), 1, 2);
        Quotation quot1 = new Quotation("2023-01-01", new HashMap<>(), opts, "true", "WEB", 12);
        Quotation quot2 = new Quotation("2023-01-01", new HashMap<>(), opts, "true", "WEB", 12);

        assertThat(quot1).hasSameHashCodeAs(quot2);
    }

    @Test
    void shouldHandleNullValues() {
        Quotation quotation = new Quotation(null, null, null, null, null, null);

        assertThat(quotation.creationDate()).isNull();
        assertThat(quotation.customers()).isNull();
        assertThat(quotation.options()).isNull();
    }

    @Test
    void shouldHandlePopulatedCustomersMap() {
        Map<String, Customer> customers = new HashMap<>();
        customers.put("CUST001", new Customer("CUST001", "123 Main", null, 1, 2, "Paris", "75001", "FR", 1,
            "John", "Doe", "Smith", "1985-05-20", "75000", "Paris", "FR", 1, "0612345678", "0145678901", 
            "0198765432", "john@example.com", 1, "Engineer", 1000, true, true, true, 1, true, true, 
            true, 1, true, true, false, 1, null, "100", 500, true));
        
        Quotation quotation = new Quotation("2023-03-01", customers, new QuotationOptions("BANK", true, "2023-03-01", new HashMap<>(), 1, 1), "true", "WEB", 12);

        assertThat(quotation.customers()).hasSize(1);
    }

    @Test
    void shouldContainFieldValuesInToString() {
        Quotation quotation = new Quotation("2023-04-01", new HashMap<>(), 
            new QuotationOptions("BANK", true, "2023-04-01", new HashMap<>(), 1, 1), "true", "MOBILE", 6);
        String toStringResult = quotation.toString();

        assertThat(toStringResult).contains("Quotation");
    }
}
