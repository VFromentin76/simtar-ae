package fr.hm.tarificateur.domain.model;

import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class QuotationOptionsTest {

    @Test
    void shouldCreateRecordWithValidValues() {
        String bank = "BNP_PARIBAS";
        Boolean couple = true;
        String effectiveDate = "2023-01-01";
        Map<String, Loan> loans = new HashMap<>();
        Integer projectQualification = 1;
        Integer projectState = 2;

        QuotationOptions result = new QuotationOptions(bank, couple, effectiveDate, loans, projectQualification, projectState);

        assertThat(result).isNotNull();
        assertThat(result.bank()).isEqualTo("BNP_PARIBAS");
        assertThat(result.couple()).isTrue();
        assertThat(result.effectiveDate()).isEqualTo("2023-01-01");
        assertThat(result.loans()).isEmpty();
        assertThat(result.projectQualification()).isEqualTo(1);
        assertThat(result.projectState()).isEqualTo(2);
    }

    @Test
    void shouldReturnCorrectGetterValues() {
        QuotationOptions qo = new QuotationOptions("CREDIT_AGRICOLE", false, "2023-02-01", new HashMap<>(), 2, 3);

        assertThat(qo.bank()).isEqualTo("CREDIT_AGRICOLE");
        assertThat(qo.couple()).isFalse();
        assertThat(qo.effectiveDate()).isEqualTo("2023-02-01");
        assertThat(qo.projectQualification()).isEqualTo(2);
        assertThat(qo.projectState()).isEqualTo(3);
    }

    @Test
    void shouldBeEqualWhenSameValues() {
        QuotationOptions qo1 = new QuotationOptions("BNP_PARIBAS", true, "2023-01-01", new HashMap<>(), 1, 2);
        QuotationOptions qo2 = new QuotationOptions("BNP_PARIBAS", true, "2023-01-01", new HashMap<>(), 1, 2);

        assertThat(qo1).isEqualTo(qo2);
    }

    @Test
    void shouldNotBeEqualWhenDifferentValues() {
        QuotationOptions qo1 = new QuotationOptions("BNP_PARIBAS", true, "2023-01-01", new HashMap<>(), 1, 2);
        QuotationOptions qo2 = new QuotationOptions("CREDIT_AGRICOLE", false, "2023-02-01", new HashMap<>(), 2, 3);

        assertThat(qo1).isNotEqualTo(qo2);
    }

    @Test
    void shouldHaveSameHashCodeForEqualObjects() {
        QuotationOptions qo1 = new QuotationOptions("BNP_PARIBAS", true, "2023-01-01", new HashMap<>(), 1, 2);
        QuotationOptions qo2 = new QuotationOptions("BNP_PARIBAS", true, "2023-01-01", new HashMap<>(), 1, 2);

        assertThat(qo1).hasSameHashCodeAs(qo2);
    }

    @Test
    void shouldContainFieldValuesInToString() {
        QuotationOptions qo = new QuotationOptions("SOCIETE_GENERALE", true, "2023-03-01", new HashMap<>(), 3, 4);
        String toStringResult = qo.toString();

        assertThat(toStringResult).contains("QuotationOptions");
    }

    @Test
    void shouldHandleNullValues() {
        QuotationOptions qo = new QuotationOptions(null, null, null, null, null, null);

        assertThat(qo.bank()).isNull();
        assertThat(qo.couple()).isNull();
        assertThat(qo.effectiveDate()).isNull();
        assertThat(qo.loans()).isNull();
        assertThat(qo.projectQualification()).isNull();
        assertThat(qo.projectState()).isNull();
    }

    @Test
    void shouldHandlePopulatedLoansMap() {
        Map<String, Loan> loans = new HashMap<>();
        Loan loan1 = new Loan("100000", "15", "MONTHS", "0", 12345, "2.5", 1, 1, new HashMap<>());
        loans.put("LOAN1", loan1);
        
        QuotationOptions qo = new QuotationOptions("BNP_PARIBAS", true, "2023-01-01", loans, 1, 2);

        assertThat(qo.loans()).hasSize(1);
        assertThat(qo.loans().get("LOAN1")).isEqualTo(loan1);
    }

    @Test
    void shouldMapProjectQualificationCodeForKnownValues() {
        assertThat(QuotationOptions.mapProjectQualificationCode(null)).isNull();
        assertThat(QuotationOptions.mapProjectQualificationCode(1)).isEqualTo("RESIDENCE_PRINCIPALE");
        assertThat(QuotationOptions.mapProjectQualificationCode(2)).isEqualTo("RESIDENCE_SECONDAIRE");
        assertThat(QuotationOptions.mapProjectQualificationCode(3)).isEqualTo("INVESTISSEMENT_LOCATIF");
        assertThat(QuotationOptions.mapProjectQualificationCode(4)).isEqualTo("TRAVAUX");
        assertThat(QuotationOptions.mapProjectQualificationCode(5)).isEqualTo("RACHAT_PRET");
        assertThat(QuotationOptions.mapProjectQualificationCode(6)).isEqualTo("AUTRE");
        assertThat(QuotationOptions.mapProjectQualificationCode(7)).isEqualTo("PRET_CONSO");
        assertThat(QuotationOptions.mapProjectQualificationCode(8)).isEqualTo("RAC_DOMINANTE_CONSOMMATION");
        assertThat(QuotationOptions.mapProjectQualificationCode(9)).isEqualTo("RAC_DOMINANTE_IMMOBILIERE");
        assertThat(QuotationOptions.mapProjectQualificationCode(10)).isEqualTo("PRET_PERSONNEL");
        assertThat(QuotationOptions.mapProjectQualificationCode(11)).isEqualTo("PRET_PROFESSIONNEL_USAGE_HABITATION");
        assertThat(QuotationOptions.mapProjectQualificationCode(12)).isEqualTo("CREDIT_BAIL");
        assertThat(QuotationOptions.mapProjectQualificationCode(99)).isNull();
    }
}

