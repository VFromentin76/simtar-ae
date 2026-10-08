package fr.hm.tarificateur.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class CustomerTest {

    @Test
    void shouldCreateRecordWithValidValues() {
        String partnerCustomerRef = "CUST001";
        String address = "123 Rue de la Paix";
        Beneficiary beneficiaries = new Beneficiary("Paris", "1980-01-15", "Jean", "Dupont", 1);
        Integer beneficiaryClause = 1;
        Integer beneficiaryType = 2;
        String city = "Paris";

        Customer result = new Customer(partnerCustomerRef, address, beneficiaries, beneficiaryClause, beneficiaryType, 
            city, "75001", "FR", 1, "John", "Doe", "Smith", "1985-05-20", "75000", "Paris", "FR", 
            1, "0612345678", "0145678901", "0198765432", "john@example.com", 1, "Engineer", 1000, 
            true, true, true, 1, true, true, true, 1, true, true, false, 1, new Address("42 Avenue", "Lyon", "2023-06-20", "69000"),
            "100", 500, true);

        assertThat(result).isNotNull();
        assertThat(result.partnerCustomerRef()).isEqualTo("CUST001");
        assertThat(result.address()).isEqualTo("123 Rue de la Paix");
        assertThat(result.city()).isEqualTo("Paris");
    }

    @Test
    void shouldReturnCorrectGetterValues() {
        Customer customer = new Customer("CUST002", "456 Avenue", null, 2, 3, 
            "Lyon", "69001", "FR", 2, "Jane", "Smith", "Martin", "1990-03-10", "69000", "Lyon", "FR", 
            2, "0687654321", "0469876543", "0412345678", "jane@example.com", 2, "Doctor", 2000, 
            false, false, false, 2, false, false, false, 2, false, false, true, 2, null, 
            "200", 1000, false);

        assertThat(customer.partnerCustomerRef()).isEqualTo("CUST002");
        assertThat(customer.firstname()).isEqualTo("Jane");
        assertThat(customer.lastname()).isEqualTo("Smith");
    }

    @Test
    void shouldBeEqualWhenSameValues() {
        Customer cust1 = new Customer("CUST001", "123 Main", null, 1, 2, "Paris", "75001", "FR", 1,
            "John", "Doe", "Smith", "1985-05-20", "75000", "Paris", "FR", 1, "0612345678", "0145678901", 
            "0198765432", "john@example.com", 1, "Engineer", 1000, true, true, true, 1, true, true, 
            true, 1, true, true, false, 1, null, "100", 500, true);
        Customer cust2 = new Customer("CUST001", "123 Main", null, 1, 2, "Paris", "75001", "FR", 1,
            "John", "Doe", "Smith", "1985-05-20", "75000", "Paris", "FR", 1, "0612345678", "0145678901", 
            "0198765432", "john@example.com", 1, "Engineer", 1000, true, true, true, 1, true, true, 
            true, 1, true, true, false, 1, null, "100", 500, true);

        assertThat(cust1).isEqualTo(cust2);
    }

    @Test
    void shouldHandleNullValues() {
        Customer customer = new Customer(null, null, null, null, null, null, null, null, null, null, null, 
            null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, 
            null, null, null, null, null, null, null, null, null, null, null, null, null, null);

        assertThat(customer.partnerCustomerRef()).isNull();
        assertThat(customer.address()).isNull();
        assertThat(customer.city()).isNull();
    }

    @Test
    void shouldHaveSameHashCodeForEqualObjects() {
        Customer cust1 = new Customer("CUST001", "123 Main", null, 1, 2, "Paris", "75001", "FR", 1,
            "John", "Doe", "Smith", "1985-05-20", "75000", "Paris", "FR", 1, "0612345678", "0145678901", 
            "0198765432", "john@example.com", 1, "Engineer", 1000, true, true, true, 1, true, true, 
            true, 1, true, true, false, 1, null, "100", 500, true);
        Customer cust2 = new Customer("CUST001", "123 Main", null, 1, 2, "Paris", "75001", "FR", 1,
            "John", "Doe", "Smith", "1985-05-20", "75000", "Paris", "FR", 1, "0612345678", "0145678901", 
            "0198765432", "john@example.com", 1, "Engineer", 1000, true, true, true, 1, true, true, 
            true, 1, true, true, false, 1, null, "100", 500, true);

        assertThat(cust1).hasSameHashCodeAs(cust2);
    }

    @Test
    void shouldContainFieldValuesInToString() {
        Customer customer = new Customer("CUST003", "789 Rue", null, 3, 4, "Marseille", "13000", "FR", 3,
            "Pierre", "Bernard", "Dupont", "1995-07-10", "13000", "Marseille", "FR", 3, "0699999999", 
            "0413131313", "0412121212", "pierre@example.com", 3, "Lawyer", 3000, true, false, true, 3, 
            false, true, false, 3, true, false, true, 3, null, "300", 1500, false);
        String toStringResult = customer.toString();

        assertThat(toStringResult).contains("Customer");
    }

    @ParameterizedTest
    @CsvSource({
        "1, SANS_ACTIVITE", "2, OUVRIERS", "3, ARTISANS", "4, CHAUFFEURS",
        "5, COMMERCANTS_CHEFS_ENTREPRISE", "6, EMPLOYES", "7, INGENIEURS_CADRES",
        "8, POLICIERS_MILITAIRES_SURVEILLANCE", "9, PROFESSIONS_AGRICOLES",
        "10, PROFESSIONS_SANTE_SOCIAL", "11, PROFESSIONS_ENSEIGNEMENT_SCIENTIFIQUES",
        "12, PROFESSIONS_INFO_ARTS_SPECTACLES", "13, PROFESSIONS_LIBERALES",
        "14, TECHNICIENS_AGENTS_MAITRISE"
    })
    void shouldMapProfessionCodes(int code, String expected) {
        assertThat(Customer.mapProfessionCode(code)).isEqualTo(expected);
    }

    @Test
    void shouldReturnNullForNullOrUnknownProfession() {
        assertThat(Customer.mapProfessionCode(null)).isNull();
        assertThat(Customer.mapProfessionCode(0)).isNull();
        assertThat(Customer.mapProfessionCode(-1)).isNull();
        assertThat(Customer.mapProfessionCode(15)).isNull();
        assertThat(Customer.mapProfessionCode(100)).isNull();
    }

    @Test
    void shouldMapFranchiseCodeForKnownValues() {
        assertThat(Customer.mapFranchiseCode(30)).isEqualTo("FR_30J");
        assertThat(Customer.mapFranchiseCode(60)).isEqualTo("FR_60J");
        assertThat(Customer.mapFranchiseCode(90)).isEqualTo("FR_90J");
        assertThat(Customer.mapFranchiseCode(120)).isEqualTo("FR_120J");
        assertThat(Customer.mapFranchiseCode(180)).isEqualTo("FR_180J");
    }

    @Test
    void shouldReturnNullForNullOrUnknownFranchise() {
        assertThat(Customer.mapFranchiseCode(null)).isNull();
        assertThat(Customer.mapFranchiseCode(0)).isNull();
    }
}
