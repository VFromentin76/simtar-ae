package fr.hm.tarificateur.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ScheduleLineTest {

    @Test
    void shouldExposeAllScheduleValues() {
        ScheduleLine line = new ScheduleLine(
            2025, 91.90, 38.90, 5.99, 12.57, 8.50, 6.20, 3.10, 2.40, 1.80, 171.36, "prorata");

        assertThat(line.year()).isEqualTo(2025);
        assertThat(line.dcPtia()).isEqualTo(91.90);
        assertThat(line.itt()).isEqualTo(38.90);
        assertThat(line.ipt()).isEqualTo(5.99);
        assertThat(line.ipp()).isEqualTo(12.57);
        assertThat(line.ip()).isEqualTo(8.50);
        assertThat(line.itp()).isEqualTo(6.20);
        assertThat(line.dos()).isEqualTo(3.10);
        assertThat(line.psy()).isEqualTo(2.40);
        assertThat(line.pe()).isEqualTo(1.80);
        assertThat(line.total()).isEqualTo(171.36);
        assertThat(line.comment()).isEqualTo("prorata");
    }

    @Test
    void shouldCompareEqualScheduleLinesByValue() {
        ScheduleLine first = new ScheduleLine(
            2025, 91.90, 38.90, 5.99, 12.57, 8.50, 6.20, 3.10, 2.40, 1.80, 171.36, null);
        ScheduleLine second = new ScheduleLine(
            2025, 91.90, 38.90, 5.99, 12.57, 8.50, 6.20, 3.10, 2.40, 1.80, 171.36, null);

        assertThat(first).isEqualTo(second).hasSameHashCodeAs(second);
        assertThat(first.toString()).contains("2025", "171.36");
    }
}
