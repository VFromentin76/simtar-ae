package fr.hm.tarificateur.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class DetailConfigTest {

    @Test
    void shouldExposeLemoineThresholdsAndCapitalLimits() {
        DetailConfig detail = new DetailConfig(
            "ASSURANCE", "ASSOC001", null, null, null, null, null, false, null,
            9.0, 10.0, 22.0, 3.0, 0.0, 0.0, 100.0, 200000.0, 110.0, 100.0,
            60, 200000.0, 1000.0, 1000000.0);

        assertThat(detail.lemoineSeuilAgeTermePret()).isEqualTo(60);
        assertThat(detail.lemoineSeuilCapitalAssureEur()).isEqualTo(200000.0);
        assertThat(detail.plafondCapitalGarantiesMinEur()).isEqualTo(1000.0);
        assertThat(detail.plafondCapitalGarantiesMaxEur()).isEqualTo(1000000.0);
    }

    @Test
    void shouldCreateRecordWithValidValues() {
        String typeContrat = "ASSURANCE_EMPRUNTEUR";
        String associationSouscriptrice = "ASSOC001";
        Reference territorialiteAdherent = new Reference("TERR001", "Metropolitan France");
        Reference territorialiteBienFinance = new Reference("TERR001", "Metropolitan France");
        Reference territorialitePrestations = new Reference("TERR001", "Metropolitan France");
        String echeanceAnniversaireContrat = "01/01";
        String echeanceAnniversaireAdhesion = "01/01";
        Boolean booIndexationGarantiesPrimes = true;
        Reference modeFractionnement = new Reference("MF001", "Monthly");
        Double tauxTaxEnsembleGaranties = 0.05;
        Double tauxChargementFraisGestion = 0.03;
        Double tauxChargementFraisAcquisition = 0.02;
        Double tauxChargementFraisAssureur = 0.01;
        Double fraisAssociationEur = 10.0;
        Double fraisDossierEur = 15.0;
        Double coefficientExonerationCotisationsEnsembleGaranties = 0.9;
        Double seuilCoefficientPassageGrosCapital = 500000.0;
        Double coefficientPassageGrosCapital = 1.1;
        Double coefficientPassageCouple = 0.95;

        DetailConfig result = new DetailConfig(typeContrat, associationSouscriptrice, territorialiteAdherent, 
            territorialiteBienFinance, territorialitePrestations, echeanceAnniversaireContrat, 
            echeanceAnniversaireAdhesion, booIndexationGarantiesPrimes, modeFractionnement, 
            tauxTaxEnsembleGaranties, tauxChargementFraisGestion, tauxChargementFraisAcquisition, 
            tauxChargementFraisAssureur, fraisAssociationEur, fraisDossierEur, 
            coefficientExonerationCotisationsEnsembleGaranties, seuilCoefficientPassageGrosCapital, 
            coefficientPassageGrosCapital, coefficientPassageCouple);

        assertThat(result).isNotNull();
        assertThat(result.typeContrat()).isEqualTo("ASSURANCE_EMPRUNTEUR");
        assertThat(result.associationSouscriptrice()).isEqualTo("ASSOC001");
    }

    @Test
    void shouldReturnCorrectGetterValues() {
        Reference terr = new Reference("TERR001", "Metropolitan France");
        Reference mf = new Reference("MF001", "Monthly");
        DetailConfig dc = new DetailConfig("ASSURANCE", "ASSOC001", terr, terr, terr, "01/01", "01/01", 
            true, mf, 0.05, 0.03, 0.02, 0.01, 10.0, 15.0, 0.9, 500000.0, 1.1, 0.95);

        assertThat(dc.typeContrat()).isEqualTo("ASSURANCE");
        assertThat(dc.booIndexationGarantiesPrimes()).isTrue();
        assertThat(dc.tauxTaxEnsembleGaranties()).isEqualTo(0.05);
    }

    @Test
    void shouldBeEqualWhenSameValues() {
        Reference terr = new Reference("TERR001", "Metropolitan France");
        Reference mf = new Reference("MF001", "Monthly");
        DetailConfig dc1 = new DetailConfig("ASSURANCE", "ASSOC001", terr, terr, terr, "01/01", "01/01", 
            true, mf, 0.05, 0.03, 0.02, 0.01, 10.0, 15.0, 0.9, 500000.0, 1.1, 0.95);
        DetailConfig dc2 = new DetailConfig("ASSURANCE", "ASSOC001", terr, terr, terr, "01/01", "01/01", 
            true, mf, 0.05, 0.03, 0.02, 0.01, 10.0, 15.0, 0.9, 500000.0, 1.1, 0.95);

        assertThat(dc1).isEqualTo(dc2);
    }

    @Test
    void shouldHandleNullValues() {
        DetailConfig dc = new DetailConfig(null, null, null, null, null, null, null, null, null, 
            null, null, null, null, null, null, null, null, null, null);

        assertThat(dc.typeContrat()).isNull();
        assertThat(dc.associationSouscriptrice()).isNull();
    }

    @Test
    void shouldHaveSameHashCodeForEqualObjects() {
        Reference terr = new Reference("TERR001", "Metropolitan France");
        Reference mf = new Reference("MF001", "Monthly");
        DetailConfig dc1 = new DetailConfig("ASSURANCE", "ASSOC001", terr, terr, terr, "01/01", "01/01", 
            true, mf, 0.05, 0.03, 0.02, 0.01, 10.0, 15.0, 0.9, 500000.0, 1.1, 0.95);
        DetailConfig dc2 = new DetailConfig("ASSURANCE", "ASSOC001", terr, terr, terr, "01/01", "01/01", 
            true, mf, 0.05, 0.03, 0.02, 0.01, 10.0, 15.0, 0.9, 500000.0, 1.1, 0.95);

        assertThat(dc1).hasSameHashCodeAs(dc2);
    }
}
