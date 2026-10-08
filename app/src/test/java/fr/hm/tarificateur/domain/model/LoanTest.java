package fr.hm.tarificateur.domain.model;

import org.junit.jupiter.api.Test;
import java.util.Map;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.*;

class LoanTest {

    @Test
    void shouldCreateRecordWithValidValues() {
        String amount = "100000";
        String duration = "15";
        String delayType = "MONTHS";
        String delay = "0";
        Integer partnerLoanRef = 12345;
        String rate = "2.5";
        Integer rateType = 1;
        Integer type = 1;
        Map<String, Warranty> warranties = new HashMap<>();

        Loan result = new Loan(amount, duration, delayType, delay, partnerLoanRef, rate, rateType, type, warranties);

        assertThat(result).isNotNull();
        assertThat(result.amount()).isEqualTo("100000");
        assertThat(result.duration()).isEqualTo("15");
        assertThat(result.delayType()).isEqualTo("MONTHS");
        assertThat(result.delay()).isEqualTo("0");
        assertThat(result.partnerLoanRef()).isEqualTo(12345);
        assertThat(result.rate()).isEqualTo("2.5");
        assertThat(result.rateType()).isEqualTo(1);
        assertThat(result.type()).isEqualTo(1);
        assertThat(result.warranties()).isEmpty();
    }

    @Test
    void shouldReturnCorrectGetterValues() {
        Map<String, Warranty> warranties = new HashMap<>();
        warranties.put("WARRANTY1", new Warranty(true, false, true, false, true, false, true, false, "50000", "50000"));
        
        Loan loan = new Loan("200000", "20", "YEARS", "1", 67890, "3.0", 2, 2, warranties);

        assertThat(loan.amount()).isEqualTo("200000");
        assertThat(loan.duration()).isEqualTo("20");
        assertThat(loan.delayType()).isEqualTo("YEARS");
        assertThat(loan.delay()).isEqualTo("1");
        assertThat(loan.partnerLoanRef()).isEqualTo(67890);
        assertThat(loan.rate()).isEqualTo("3.0");
        assertThat(loan.rateType()).isEqualTo(2);
        assertThat(loan.type()).isEqualTo(2);
        assertThat(loan.warranties()).isNotEmpty();
        assertThat(loan.warranties()).hasSize(1);
    }

    @Test
    void shouldBeEqualWhenSameValues() {
        Map<String, Warranty> warranties1 = new HashMap<>();
        Map<String, Warranty> warranties2 = new HashMap<>();
        
        Loan loan1 = new Loan("100000", "15", "MONTHS", "0", 12345, "2.5", 1, 1, warranties1);
        Loan loan2 = new Loan("100000", "15", "MONTHS", "0", 12345, "2.5", 1, 1, warranties2);

        assertThat(loan1).isEqualTo(loan2);
    }

    @Test
    void shouldNotBeEqualWhenDifferentValues() {
        Loan loan1 = new Loan("100000", "15", "MONTHS", "0", 12345, "2.5", 1, 1, new HashMap<>());
        Loan loan2 = new Loan("200000", "20", "YEARS", "1", 67890, "3.0", 2, 2, new HashMap<>());

        assertThat(loan1).isNotEqualTo(loan2);
    }

    @Test
    void shouldHaveSameHashCodeForEqualObjects() {
        Loan loan1 = new Loan("100000", "15", "MONTHS", "0", 12345, "2.5", 1, 1, new HashMap<>());
        Loan loan2 = new Loan("100000", "15", "MONTHS", "0", 12345, "2.5", 1, 1, new HashMap<>());

        assertThat(loan1).hasSameHashCodeAs(loan2);
    }

    @Test
    void shouldContainFieldValuesInToString() {
        Loan loan = new Loan("150000", "18", "DAYS", "2", 54321, "2.8", 3, 3, new HashMap<>());
        String toStringResult = loan.toString();

        assertThat(toStringResult).contains("Loan", "150000");
    }

    @Test
    void shouldHandleNullValues() {
        Loan loan = new Loan(null, null, null, null, null, null, null, null, null);

        assertThat(loan.amount()).isNull();
        assertThat(loan.duration()).isNull();
        assertThat(loan.delayType()).isNull();
        assertThat(loan.delay()).isNull();
        assertThat(loan.partnerLoanRef()).isNull();
        assertThat(loan.rate()).isNull();
        assertThat(loan.rateType()).isNull();
        assertThat(loan.type()).isNull();
        assertThat(loan.warranties()).isNull();
    }

    @Test
    void shouldHandlePopulatedWarrantiesMap() {
        Map<String, Warranty> warranties = new HashMap<>();
        Warranty w1 = new Warranty(true, true, false, true, false, true, true, false, "100000", "100000");
        Warranty w2 = new Warranty(false, true, true, false, true, false, false, true, "50000", "50000");
        warranties.put("WARRANTY1", w1);
        warranties.put("WARRANTY2", w2);
        
        Loan loan = new Loan("300000", "25", "MONTHS", "3", 99999, "3.5", 4, 4, warranties);

        assertThat(loan.warranties()).hasSize(2);
        assertThat(loan.warranties().get("WARRANTY1")).isEqualTo(w1);
        assertThat(loan.warranties().get("WARRANTY2")).isEqualTo(w2);
    }

    @Test
    void shouldMapTypeCodeForKnownValues() {
        assertThat(Loan.mapTypeCode(null)).isNull();
        assertThat(Loan.mapTypeCode(1)).isEqualTo("PRET_AMORTISSABLE");
        assertThat(Loan.mapTypeCode(2)).isEqualTo("PRET_CONSTANT");
        assertThat(Loan.mapTypeCode(3)).isEqualTo("PRET_PALLIER");
        assertThat(Loan.mapTypeCode(4)).isEqualTo("PRET_RELAIS");
        assertThat(Loan.mapTypeCode(5)).isEqualTo("PTZ");
        assertThat(Loan.mapTypeCode(6)).isEqualTo("PRET_IN_FINE");
        assertThat(Loan.mapTypeCode(99)).isNull();
    }
}

