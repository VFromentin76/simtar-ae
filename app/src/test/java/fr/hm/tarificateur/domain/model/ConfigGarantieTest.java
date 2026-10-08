package fr.hm.tarificateur.domain.model;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class ConfigGarantieTest {

    @Test
    void shouldExposeExtendedWarrantyConfiguration() {
        ExonerationCotisations exoneration = new ExonerationCotisations(
            false, List.of(new ExonerationCotisationsCoefficient(true, 105.0)));
        ConfigGarantie configuration = new ConfigGarantie(
            new Reference("GAR001", "Garantie"), true, false, 18, 65, 70,
            new Reference("FR", "France"), 2500.0, 500.0, 200000.0,
            List.of(), List.of(), List.of(), List.of(), List.of(), exoneration);

        assertThat(configuration.booGafChoisie()).isFalse();
        assertThat(configuration.ageFinCouvertureVie()).isEqualTo(70);
        assertThat(configuration.exonerationCotisations()).isEqualTo(exoneration);
    }

    @Test
    void shouldCreateRecordWithValidValues() {
        Reference garantie = new Reference("GAR001", "Garantie 1");
        Boolean booObligatoire = true;
        Integer ageAdhesionMin = 18;
        Integer ageAdhesionMax = 65;
        Reference territorialite = new Reference("TERR001", "Metropolitan France");
        Double montantMensuelMaxIndemnisableEur = 5000.0;
        Double plafondCapitalMinEur = 10000.0;
        Double plafondCapitalMaxEur = 1000000.0;
        List<Dependance> dependances = new ArrayList<>();
        List<FranchiseCoefficient> franchises = new ArrayList<>();
        List<OptionCoefficient> options = new ArrayList<>();
        List<PrimePureCi> primesPuresCi = new ArrayList<>();
        List<PrimePureCrd> primesPuresCrd = new ArrayList<>();

        ConfigGarantie result = new ConfigGarantie(garantie, booObligatoire, ageAdhesionMin, ageAdhesionMax, 
            territorialite, montantMensuelMaxIndemnisableEur, plafondCapitalMinEur, plafondCapitalMaxEur, 
            dependances, franchises, options, primesPuresCi, primesPuresCrd);

        assertThat(result).isNotNull();
        assertThat(result.garantie()).isEqualTo(garantie);
        assertThat(result.booObligatoire()).isTrue();
        assertThat(result.ageAdhesionMin()).isEqualTo(18);
        assertThat(result.ageAdhesionMax()).isEqualTo(65);
    }

    @Test
    void shouldReturnCorrectGetterValues() {
        Reference gar = new Reference("GAR002", "Garantie 2");
        Reference terr = new Reference("TERR001", "Metropolitan France");
        ConfigGarantie cg = new ConfigGarantie(gar, false, 21, 70, terr, 4000.0, 20000.0, 500000.0, 
            new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

        assertThat(cg.garantie()).isEqualTo(gar);
        assertThat(cg.booObligatoire()).isFalse();
        assertThat(cg.ageAdhesionMin()).isEqualTo(21);
        assertThat(cg.ageAdhesionMax()).isEqualTo(70);
    }

    @Test
    void shouldBeEqualWhenSameValues() {
        Reference gar = new Reference("GAR001", "Garantie 1");
        Reference terr = new Reference("TERR001", "Metropolitan France");
        ConfigGarantie cg1 = new ConfigGarantie(gar, true, 18, 65, terr, 5000.0, 10000.0, 1000000.0, 
            new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        ConfigGarantie cg2 = new ConfigGarantie(gar, true, 18, 65, terr, 5000.0, 10000.0, 1000000.0, 
            new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

        assertThat(cg1).isEqualTo(cg2);
    }

    @Test
    void shouldHandleNullValues() {
        ConfigGarantie cg = new ConfigGarantie(null, null, null, null, null, null, null, null, 
            null, null, null, null, null);

        assertThat(cg.garantie()).isNull();
        assertThat(cg.booObligatoire()).isNull();
        assertThat(cg.dependances()).isNull();
    }

    @Test
    void shouldHaveSameHashCodeForEqualObjects() {
        Reference gar = new Reference("GAR001", "Garantie 1");
        Reference terr = new Reference("TERR001", "Metropolitan France");
        ConfigGarantie cg1 = new ConfigGarantie(gar, true, 18, 65, terr, 5000.0, 10000.0, 1000000.0, 
            new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        ConfigGarantie cg2 = new ConfigGarantie(gar, true, 18, 65, terr, 5000.0, 10000.0, 1000000.0, 
            new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

        assertThat(cg1).hasSameHashCodeAs(cg2);
    }
}
