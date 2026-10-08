package fr.hm.tarificateur.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class AddressTest {

    @Test
    void shouldCreateRecordWithValidValues() {
        String address = "123 Rue de la Paix";
        String city = "Paris";
        String moveDate = "2023-01-15";
        String zipCode = "75000";

        Address result = new Address(address, city, moveDate, zipCode);

        assertThat(result).isNotNull();
        assertThat(result.address()).isEqualTo("123 Rue de la Paix");
        assertThat(result.city()).isEqualTo("Paris");
        assertThat(result.moveDate()).isEqualTo("2023-01-15");
        assertThat(result.zipCode()).isEqualTo("75000");
    }

    @Test
    void shouldReturnCorrectGetterValues() {
        Address address = new Address("42 Avenue", "Lyon", "2023-06-20", "69000");

        assertThat(address.address()).isEqualTo("42 Avenue");
        assertThat(address.city()).isEqualTo("Lyon");
        assertThat(address.moveDate()).isEqualTo("2023-06-20");
        assertThat(address.zipCode()).isEqualTo("69000");
    }

    @Test
    void shouldBeEqualWhenSameValues() {
        Address addr1 = new Address("123 Main", "Paris", "2023-01-01", "75001");
        Address addr2 = new Address("123 Main", "Paris", "2023-01-01", "75001");

        assertThat(addr1).isEqualTo(addr2);
    }

    @Test
    void shouldNotBeEqualWhenDifferentValues() {
        Address addr1 = new Address("123 Main", "Paris", "2023-01-01", "75001");
        Address addr2 = new Address("456 Side", "Lyon", "2023-02-02", "69001");

        assertThat(addr1).isNotEqualTo(addr2);
    }

    @Test
    void shouldHaveSameHashCodeForEqualObjects() {
        Address addr1 = new Address("123 Main", "Paris", "2023-01-01", "75001");
        Address addr2 = new Address("123 Main", "Paris", "2023-01-01", "75001");

        assertThat(addr1).hasSameHashCodeAs(addr2);
    }

    @Test
    void shouldHaveDifferentHashCodeForDifferentObjects() {
        Address addr1 = new Address("123 Main", "Paris", "2023-01-01", "75001");
        Address addr2 = new Address("456 Side", "Lyon", "2023-02-02", "69001");

        assertThat(addr1.hashCode()).isNotEqualTo(addr2.hashCode());
    }

    @Test
    void shouldContainFieldValuesInToString() {
        Address address = new Address("789 Rue", "Marseille", "2023-05-10", "13000");
        String toStringResult = address.toString();

        assertThat(toStringResult).contains("Address", "789 Rue", "Marseille", "2023-05-10", "13000");
    }

    @Test
    void shouldHandleNullValues() {
        Address address = new Address(null, null, null, null);

        assertThat(address.address()).isNull();
        assertThat(address.city()).isNull();
        assertThat(address.moveDate()).isNull();
        assertThat(address.zipCode()).isNull();
    }

    @Test
    void shouldHandleEmptyStrings() {
        Address address = new Address("", "", "", "");

        assertThat(address.address()).isEmpty();
        assertThat(address.city()).isEmpty();
        assertThat(address.moveDate()).isEmpty();
        assertThat(address.zipCode()).isEmpty();
    }
}

