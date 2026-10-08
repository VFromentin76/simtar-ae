package fr.hm.tarificateur.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ProductWarrantyFactorsTest {

    @Test
    void shouldCreateRecordWithValidValues() {
        Double ipt = 0.8;
        Double itt = 0.7;
        Double ip = 0.9;
        Double ipp = 0.85;

        ProductWarrantyFactors result = new ProductWarrantyFactors(ipt, itt, ip, ipp);

        assertThat(result).isNotNull();
        assertThat(result.ipt()).isEqualTo(0.8);
        assertThat(result.itt()).isEqualTo(0.7);
        assertThat(result.ip()).isEqualTo(0.9);
        assertThat(result.ipp()).isEqualTo(0.85);
    }

    @Test
    void shouldReturnCorrectGetterValues() {
        ProductWarrantyFactors pwf = new ProductWarrantyFactors(0.75, 0.65, 0.95, 0.80);

        assertThat(pwf.ipt()).isEqualTo(0.75);
        assertThat(pwf.itt()).isEqualTo(0.65);
        assertThat(pwf.ip()).isEqualTo(0.95);
        assertThat(pwf.ipp()).isEqualTo(0.80);
    }

    @Test
    void shouldBeEqualWhenSameValues() {
        ProductWarrantyFactors pwf1 = new ProductWarrantyFactors(0.8, 0.7, 0.9, 0.85);
        ProductWarrantyFactors pwf2 = new ProductWarrantyFactors(0.8, 0.7, 0.9, 0.85);

        assertThat(pwf1).isEqualTo(pwf2);
    }

    @Test
    void shouldNotBeEqualWhenDifferentValues() {
        ProductWarrantyFactors pwf1 = new ProductWarrantyFactors(0.8, 0.7, 0.9, 0.85);
        ProductWarrantyFactors pwf2 = new ProductWarrantyFactors(0.75, 0.65, 0.95, 0.80);

        assertThat(pwf1).isNotEqualTo(pwf2);
    }

    @Test
    void shouldHaveSameHashCodeForEqualObjects() {
        ProductWarrantyFactors pwf1 = new ProductWarrantyFactors(0.8, 0.7, 0.9, 0.85);
        ProductWarrantyFactors pwf2 = new ProductWarrantyFactors(0.8, 0.7, 0.9, 0.85);

        assertThat(pwf1).hasSameHashCodeAs(pwf2);
    }

    @Test
    void shouldContainFieldValuesInToString() {
        ProductWarrantyFactors pwf = new ProductWarrantyFactors(0.82, 0.72, 0.92, 0.87);
        String toStringResult = pwf.toString();

        assertThat(toStringResult).contains("ProductWarrantyFactors");
    }

    @Test
    void shouldHandleNullValues() {
        ProductWarrantyFactors pwf = new ProductWarrantyFactors(null, null, null, null);

        assertThat(pwf.ipt()).isNull();
        assertThat(pwf.itt()).isNull();
        assertThat(pwf.ip()).isNull();
        assertThat(pwf.ipp()).isNull();
    }

    @Test
    void shouldHandleZeroValues() {
        ProductWarrantyFactors pwf = new ProductWarrantyFactors(0.0, 0.0, 0.0, 0.0);

        assertThat(pwf.ipt()).isEqualTo(0.0);
        assertThat(pwf.itt()).isEqualTo(0.0);
        assertThat(pwf.ip()).isEqualTo(0.0);
        assertThat(pwf.ipp()).isEqualTo(0.0);
    }

    @Test
    void shouldHandleLargeValues() {
        ProductWarrantyFactors pwf = new ProductWarrantyFactors(99.99, 99.99, 99.99, 99.99);

        assertThat(pwf.ipt()).isEqualTo(99.99);
        assertThat(pwf.itt()).isEqualTo(99.99);
        assertThat(pwf.ip()).isEqualTo(99.99);
        assertThat(pwf.ipp()).isEqualTo(99.99);
    }

    @Test
    void shouldHandleOneValues() {
        ProductWarrantyFactors pwf = new ProductWarrantyFactors(1.0, 1.0, 1.0, 1.0);

        assertThat(pwf.ipt()).isEqualTo(1.0);
        assertThat(pwf.itt()).isEqualTo(1.0);
        assertThat(pwf.ip()).isEqualTo(1.0);
        assertThat(pwf.ipp()).isEqualTo(1.0);
    }
}

