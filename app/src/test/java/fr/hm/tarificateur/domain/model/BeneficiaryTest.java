package fr.hm.tarificateur.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class BeneficiaryTest {

    @Test
    void shouldCreateRecordWithValidValues() {
        String birthCity = "Paris";
        String birthDate = "1990-05-15";
        String firstname = "Jean";
        String lastname = "Dupont";
        Integer order = 1;

        Beneficiary result = new Beneficiary(birthCity, birthDate, firstname, lastname, order);

        assertThat(result).isNotNull();
        assertThat(result.birthCity()).isEqualTo("Paris");
        assertThat(result.birthDate()).isEqualTo("1990-05-15");
        assertThat(result.firstname()).isEqualTo("Jean");
        assertThat(result.lastname()).isEqualTo("Dupont");
        assertThat(result.order()).isEqualTo(1);
    }

    @Test
    void shouldReturnCorrectGetterValues() {
        Beneficiary beneficiary = new Beneficiary("Lyon", "1985-03-20", "Marie", "Martin", 2);

        assertThat(beneficiary.birthCity()).isEqualTo("Lyon");
        assertThat(beneficiary.birthDate()).isEqualTo("1985-03-20");
        assertThat(beneficiary.firstname()).isEqualTo("Marie");
        assertThat(beneficiary.lastname()).isEqualTo("Martin");
        assertThat(beneficiary.order()).isEqualTo(2);
    }

    @Test
    void shouldBeEqualWhenSameValues() {
        Beneficiary ben1 = new Beneficiary("Paris", "1990-05-15", "Jean", "Dupont", 1);
        Beneficiary ben2 = new Beneficiary("Paris", "1990-05-15", "Jean", "Dupont", 1);

        assertThat(ben1).isEqualTo(ben2);
    }

    @Test
    void shouldNotBeEqualWhenDifferentValues() {
        Beneficiary ben1 = new Beneficiary("Paris", "1990-05-15", "Jean", "Dupont", 1);
        Beneficiary ben2 = new Beneficiary("Lyon", "1985-03-20", "Marie", "Martin", 2);

        assertThat(ben1).isNotEqualTo(ben2);
    }

    @Test
    void shouldHaveSameHashCodeForEqualObjects() {
        Beneficiary ben1 = new Beneficiary("Paris", "1990-05-15", "Jean", "Dupont", 1);
        Beneficiary ben2 = new Beneficiary("Paris", "1990-05-15", "Jean", "Dupont", 1);

        assertThat(ben1).hasSameHashCodeAs(ben2);
    }

    @Test
    void shouldHaveDifferentHashCodeForDifferentObjects() {
        Beneficiary ben1 = new Beneficiary("Paris", "1990-05-15", "Jean", "Dupont", 1);
        Beneficiary ben2 = new Beneficiary("Lyon", "1985-03-20", "Marie", "Martin", 2);

        assertThat(ben1.hashCode()).isNotEqualTo(ben2.hashCode());
    }

    @Test
    void shouldContainFieldValuesInToString() {
        Beneficiary beneficiary = new Beneficiary("Marseille", "1995-07-20", "Pierre", "Bernard", 3);
        String toStringResult = beneficiary.toString();

        assertThat(toStringResult).contains("Beneficiary", "Marseille", "1995-07-20", "Pierre", "Bernard", "3");
    }

    @Test
    void shouldHandleNullValues() {
        Beneficiary beneficiary = new Beneficiary(null, null, null, null, null);

        assertThat(beneficiary.birthCity()).isNull();
        assertThat(beneficiary.birthDate()).isNull();
        assertThat(beneficiary.firstname()).isNull();
        assertThat(beneficiary.lastname()).isNull();
        assertThat(beneficiary.order()).isNull();
    }

    @Test
    void shouldHandleEmptyStrings() {
        Beneficiary beneficiary = new Beneficiary("", "", "", "", 0);

        assertThat(beneficiary.birthCity()).isEmpty();
        assertThat(beneficiary.birthDate()).isEmpty();
        assertThat(beneficiary.firstname()).isEmpty();
        assertThat(beneficiary.lastname()).isEmpty();
        assertThat(beneficiary.order()).isEqualTo(0);
    }

    @Test
    void shouldHandleVariousOrderValues() {
        Beneficiary ben1 = new Beneficiary("Paris", "1990-05-15", "Jean", "Dupont", 0);
        Beneficiary ben2 = new Beneficiary("Lyon", "1985-03-20", "Marie", "Martin", 100);

        assertThat(ben1.order()).isEqualTo(0);
        assertThat(ben2.order()).isEqualTo(100);
    }
}

