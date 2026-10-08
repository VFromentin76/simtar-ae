package fr.hm.tarificateur.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ProfessionClasseRisqueMappingTest {

    @Test
    void shouldCreateRecordWithValidValues() {
        Reference profession = new Reference("PROF001", "Profession 1");
        Reference classeRisque = new Reference("CR001", "Classe Risque 1");

        ProfessionClasseRisqueMapping result =
            new ProfessionClasseRisqueMapping(profession, "VIE", classeRisque);

        assertThat(result).isNotNull();
        assertThat(result.profession()).isEqualTo(profession);
        assertThat(result.branche()).isEqualTo("VIE");
        assertThat(result.classeRisque()).isEqualTo(classeRisque);
    }

    @Test
    void shouldReturnCorrectGetterValues() {
        Reference prof = new Reference("PROF002", "Profession 2");
        Reference cr = new Reference("CR002", "Classe Risque 2");
        ProfessionClasseRisqueMapping pcrm = new ProfessionClasseRisqueMapping(prof, "NON_VIE", cr);

        assertThat(pcrm.profession()).isEqualTo(prof);
        assertThat(pcrm.branche()).isEqualTo("NON_VIE");
        assertThat(pcrm.classeRisque()).isEqualTo(cr);
    }

    @Test
    void shouldBeEqualWhenSameValues() {
        Reference prof = new Reference("PROF001", "Profession 1");
        Reference cr = new Reference("CR001", "Classe Risque 1");
        ProfessionClasseRisqueMapping pcrm1 = new ProfessionClasseRisqueMapping(prof, "VIE", cr);
        ProfessionClasseRisqueMapping pcrm2 = new ProfessionClasseRisqueMapping(prof, "VIE", cr);

        assertThat(pcrm1).isEqualTo(pcrm2);
    }

    @Test
    void shouldNotBeEqualWhenDifferentValues() {
        Reference prof1 = new Reference("PROF001", "Profession 1");
        Reference prof2 = new Reference("PROF002", "Profession 2");
        Reference cr = new Reference("CR001", "Classe Risque 1");
        ProfessionClasseRisqueMapping pcrm1 = new ProfessionClasseRisqueMapping(prof1, "VIE", cr);
        ProfessionClasseRisqueMapping pcrm2 = new ProfessionClasseRisqueMapping(prof2, "VIE", cr);

        assertThat(pcrm1).isNotEqualTo(pcrm2);
    }

    @Test
    void shouldHaveSameHashCodeForEqualObjects() {
        Reference prof = new Reference("PROF001", "Profession 1");
        Reference cr = new Reference("CR001", "Classe Risque 1");
        ProfessionClasseRisqueMapping pcrm1 = new ProfessionClasseRisqueMapping(prof, "VIE", cr);
        ProfessionClasseRisqueMapping pcrm2 = new ProfessionClasseRisqueMapping(prof, "VIE", cr);

        assertThat(pcrm1).hasSameHashCodeAs(pcrm2);
    }

    @Test
    void shouldHandleNullValues() {
        ProfessionClasseRisqueMapping pcrm =
            new ProfessionClasseRisqueMapping(null, null, null);

        assertThat(pcrm.profession()).isNull();
        assertThat(pcrm.branche()).isNull();
        assertThat(pcrm.classeRisque()).isNull();
    }

    @Test
    void shouldContainFieldValuesInToString() {
        Reference prof = new Reference("PROF003", "Profession 3");
        Reference cr = new Reference("CR003", "Classe Risque 3");
        ProfessionClasseRisqueMapping pcrm = new ProfessionClasseRisqueMapping(prof, "VIE", cr);
        String toStringResult = pcrm.toString();

        assertThat(toStringResult).contains("ProfessionClasseRisqueMapping");
    }
}
