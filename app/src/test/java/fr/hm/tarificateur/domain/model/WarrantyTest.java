package fr.hm.tarificateur.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class WarrantyTest {

    @Test
    void shouldCreateRecordWithValidValues() {
        Boolean ip = true;
        Boolean ipp = true;
        Boolean ipt = false;
        Boolean itp = true;
        Boolean itt = false;
        Boolean dos = true;
        Boolean psy = true;
        Boolean pe = false;
        String quotityVie = "100000";
        String quotityNonVie = "80000";

        Warranty result = new Warranty(ip, ipp, ipt, itp, itt, dos, psy, pe, quotityVie, quotityNonVie);

        assertThat(result).isNotNull();
        assertThat(result.ip()).isTrue();
        assertThat(result.ipp()).isTrue();
        assertThat(result.ipt()).isFalse();
        assertThat(result.itp()).isTrue();
        assertThat(result.itt()).isFalse();
        assertThat(result.dos()).isTrue();
        assertThat(result.psy()).isTrue();
        assertThat(result.pe()).isFalse();
        assertThat(result.quotityVie()).isEqualTo("100000");
        assertThat(result.quotityNonVie()).isEqualTo("80000");
    }

    @Test
    void shouldReturnCorrectGetterValues() {
        Warranty warranty = new Warranty(false, true, true, false, true, false, false, true, "50000", "40000");

        assertThat(warranty.ip()).isFalse();
        assertThat(warranty.ipp()).isTrue();
        assertThat(warranty.ipt()).isTrue();
        assertThat(warranty.itp()).isFalse();
        assertThat(warranty.itt()).isTrue();
        assertThat(warranty.dos()).isFalse();
        assertThat(warranty.psy()).isFalse();
        assertThat(warranty.pe()).isTrue();
        assertThat(warranty.quotityVie()).isEqualTo("50000");
        assertThat(warranty.quotityNonVie()).isEqualTo("40000");
    }

    @Test
    void shouldBeEqualWhenSameValues() {
        Warranty w1 = new Warranty(true, true, false, true, false, true, true, false, "100000", "80000");
        Warranty w2 = new Warranty(true, true, false, true, false, true, true, false, "100000", "80000");

        assertThat(w1).isEqualTo(w2);
    }

    @Test
    void shouldNotBeEqualWhenDifferentValues() {
        Warranty w1 = new Warranty(true, true, false, true, false, true, true, false, "100000", "80000");
        Warranty w2 = new Warranty(false, true, true, false, true, false, false, true, "50000", "40000");

        assertThat(w1).isNotEqualTo(w2);
    }

    @Test
    void shouldNotBeEqualWhenOnlyQuotityNonVieDiffers() {
        Warranty w1 = new Warranty(true, true, false, true, false, true, true, false, "100000", "80000");
        Warranty w2 = new Warranty(true, true, false, true, false, true, true, false, "100000", "70000");

        assertThat(w1).isNotEqualTo(w2);
    }

    @Test
    void shouldHaveSameHashCodeForEqualObjects() {
        Warranty w1 = new Warranty(true, true, false, true, false, true, true, false, "100000", "80000");
        Warranty w2 = new Warranty(true, true, false, true, false, true, true, false, "100000", "80000");

        assertThat(w1).hasSameHashCodeAs(w2);
    }

    @Test
    void shouldContainFieldValuesInToString() {
        Warranty warranty = new Warranty(true, true, false, true, false, true, true, false, "75000", "60000");
        String toStringResult = warranty.toString();

        assertThat(toStringResult).contains("Warranty").contains("75000").contains("60000");
    }

    @Test
    void shouldHandleNullValues() {
        Warranty warranty = new Warranty(null, null, null, null, null, null, null, null, null, null);

        assertThat(warranty.ip()).isNull();
        assertThat(warranty.ipp()).isNull();
        assertThat(warranty.ipt()).isNull();
        assertThat(warranty.itp()).isNull();
        assertThat(warranty.itt()).isNull();
        assertThat(warranty.dos()).isNull();
        assertThat(warranty.psy()).isNull();
        assertThat(warranty.pe()).isNull();
        assertThat(warranty.quotityVie()).isNull();
        assertThat(warranty.quotityNonVie()).isNull();
    }

    @Test
    void shouldHandleAllFalseValues() {
        Warranty warranty = new Warranty(false, false, false, false, false, false, false, false, "0", "0");

        assertThat(warranty.ip()).isFalse();
        assertThat(warranty.ipp()).isFalse();
        assertThat(warranty.ipt()).isFalse();
        assertThat(warranty.itp()).isFalse();
        assertThat(warranty.itt()).isFalse();
        assertThat(warranty.dos()).isFalse();
        assertThat(warranty.psy()).isFalse();
        assertThat(warranty.pe()).isFalse();
    }

    @Test
    void shouldHandleAllTrueValues() {
        Warranty warranty = new Warranty(true, true, true, true, true, true, true, true, "999999", "888888");

        assertThat(warranty.ip()).isTrue();
        assertThat(warranty.ipp()).isTrue();
        assertThat(warranty.ipt()).isTrue();
        assertThat(warranty.itp()).isTrue();
        assertThat(warranty.itt()).isTrue();
        assertThat(warranty.dos()).isTrue();
        assertThat(warranty.psy()).isTrue();
        assertThat(warranty.pe()).isTrue();
    }

    @Test
    void shouldHandleEmptyQuotities() {
        Warranty warranty = new Warranty(true, false, true, false, true, false, true, false, "", "");

        assertThat(warranty.quotityVie()).isEmpty();
        assertThat(warranty.quotityNonVie()).isEmpty();
    }

    @Test
    void shouldSupportDifferentQuotityVieAndQuotityNonVie() {
        Warranty warranty = new Warranty(true, false, true, false, true, false, true, false, "100", "50");

        assertThat(warranty.quotityVie()).isEqualTo("100");
        assertThat(warranty.quotityNonVie()).isEqualTo("50");
    }

    @Test
    void shouldExposeTerritorialityOptions() {
        Warranty warranty = new Warranty(
            true, false, false, false, false, false, false, false,
            "100", "100", false, null, true, false
        );

        assertThat(warranty.drom()).isTrue();
        assertThat(warranty.corse()).isFalse();
    }

    @Test
    void shouldExposeIptCapitalOption() {
        Warranty warranty = new Warranty(
            false, false, true, false, true, false, false, false,
            "100", "100", false, null, false, false, true
        );

        assertThat(warranty.iptSortieCapital()).isTrue();
    }
}
