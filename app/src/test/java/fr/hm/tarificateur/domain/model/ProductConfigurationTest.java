package fr.hm.tarificateur.domain.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class ProductConfigurationTest {

    @Test
    void shouldExposeCompleteCiAndCrdConfigurationShape() {
        List<CoverageEndCoefficient> coverage = List.of(new CoverageEndCoefficient(35, 65, 100.0));
        List<TerritorialiteCoefficient> territories = List.of(
            new TerritorialiteCoefficient(new Reference("DROM", "DROM"), 110.0));
        List<CoefficientPerimetreLemoineCi> lemoineCi = List.of(
            new CoefficientPerimetreLemoineCi(40, "VIE", 105.0));
        List<TypePretCourbeDeformationCrd> loanCurves = List.of(
            new TypePretCourbeDeformationCrd(new Reference("AMORT", "Amortissable"), true));
        List<CoefficientPerimetreLemoineCrd> lemoineCrd = List.of(
            new CoefficientPerimetreLemoineCrd(40, "VIE", 106.0));
        List<CoefficientAerasCi> aerasCi = List.of(new CoefficientAerasCi(40, 20, 120.0));
        List<CoefficientAerasCrd> aerasCrd = List.of(new CoefficientAerasCrd(55, 121.0));
        List<ExclusionCategoriePro> exclusions = List.of(
            new ExclusionCategoriePro(new Reference("CAT", "Catégorie"), true, false));
        List<java.util.Map<String, Object>> racEligibility = List.of(java.util.Map.of("eligible", true));
        List<java.util.Map<String, Object>> racCoefficients = List.of(java.util.Map.of("coefficient", 125));

        ProductConfiguration configuration = new ProductConfiguration(
            "PROD", "CODE", "Produit", LocalDate.of(2026, 1, 1), null, "ACTIF", "CI",
            "LEMOINE_ET_HORS_LEMOINE", null, List.of(), List.of(), List.of(), List.of(),
            List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(),
            coverage, territories, lemoineCi, loanCurves, lemoineCrd, aerasCi, aerasCi,
            aerasCrd, aerasCrd, exclusions, racEligibility, racCoefficients);

        assertThat(configuration.coefficientsFinCouvertureAtCi()).isEqualTo(coverage);
        assertThat(configuration.territorialiteCoefficientsNonVie()).isEqualTo(territories);
        assertThat(configuration.coefficientsPerimetreLemoineCi()).isEqualTo(lemoineCi);
        assertThat(configuration.typePretCourbesDeformationCrd()).isEqualTo(loanCurves);
        assertThat(configuration.coefficientsPerimetreLemoineCrd()).isEqualTo(lemoineCrd);
        assertThat(configuration.coefficientsAerasRefusCi()).isEqualTo(aerasCi);
        assertThat(configuration.coefficientsAerasExclusionCi()).isEqualTo(aerasCi);
        assertThat(configuration.coefficientsAerasRefusCrd()).isEqualTo(aerasCrd);
        assertThat(configuration.coefficientsAerasExclusionCrd()).isEqualTo(aerasCrd);
        assertThat(configuration.exclusionsCategoriePro()).isEqualTo(exclusions);
        assertThat(configuration.racEligibilites()).isEqualTo(racEligibility);
        assertThat(configuration.racCoefficients()).isEqualTo(racCoefficients);
    }

    @Test
    void shouldCreateRecordWithValidValues() {
        String codeProduit = "PROD001";
        String code = "P001";
        String libelle = "Product 1";
        LocalDate dateEffetDebut = LocalDate.of(2023, 1, 1);
        LocalDate dateEffetFin = LocalDate.of(2024, 12, 31);
        String statut = "ACTIVE";
        String modeCalcul = "STANDARD";
        String eligibiliteLemoine = "YES";
        DetailConfig detailConfig = new DetailConfig("ASSURANCE", "ASSOC001", new Reference("TERR001", "France"), 
            new Reference("TERR001", "France"), new Reference("TERR001", "France"), "01/01", "01/01", 
            true, new Reference("MF001", "Monthly"), 0.05, 0.03, 0.02, 0.01, 10.0, 15.0, 0.9, 500000.0, 1.1, 0.95);
        List<ConfigGarantie> garanties = new ArrayList<>();
        List<TypePretEligibilite> typePretEligibilites = new ArrayList<>();
        List<ObjetPretEligibilite> objetPretEligibilites = new ArrayList<>();
        List<TypePretCoefficient> typePretCoefficients = new ArrayList<>();
        List<ObjetPretCoefficient> objetPretCoefficients = new ArrayList<>();
        List<ClasseRisqueCoefficient> classeRisqueCoefficients = new ArrayList<>();
        List<CourbeDeformationCrd> courbeDeformationCrd = new ArrayList<>();
        List<CoefficientPassageFumeurCi> coefficientsPassageFumeurCi = new ArrayList<>();
        List<CoefficientPassageFumeurCrd> coefficientsPassageFumeurCrd = new ArrayList<>();
        List<CategorieProClasseRisqueMapping> mappingsCategoriePro = new ArrayList<>();
        List<ProfessionClasseRisqueMapping> mappingsProfession = new ArrayList<>();

        ProductConfiguration result = new ProductConfiguration(codeProduit, code, libelle, dateEffetDebut, dateEffetFin, 
            statut, modeCalcul, eligibiliteLemoine, detailConfig, garanties, typePretEligibilites, 
            objetPretEligibilites, typePretCoefficients, objetPretCoefficients, classeRisqueCoefficients, 
            courbeDeformationCrd, coefficientsPassageFumeurCi, coefficientsPassageFumeurCrd, 
            mappingsCategoriePro, mappingsProfession);

        assertThat(result).isNotNull();
        assertThat(result.codeProduit()).isEqualTo("PROD001");
        assertThat(result.code()).isEqualTo("P001");
        assertThat(result.libelle()).isEqualTo("Product 1");
        assertThat(result.statut()).isEqualTo("ACTIVE");
    }

    @Test
    void shouldReturnCorrectGetterValues() {
        LocalDate start = LocalDate.of(2023, 1, 1);
        LocalDate end = LocalDate.of(2024, 12, 31);
        DetailConfig dc = new DetailConfig("ASSURANCE", "ASSOC001", new Reference("TERR001", "France"), 
            new Reference("TERR001", "France"), new Reference("TERR001", "France"), "01/01", "01/01", 
            true, new Reference("MF001", "Monthly"), 0.05, 0.03, 0.02, 0.01, 10.0, 15.0, 0.9, 500000.0, 1.1, 0.95);
        
        ProductConfiguration pc = new ProductConfiguration("PROD002", "P002", "Product 2", start, end, 
            "INACTIVE", "CUSTOM", "NO", dc, new ArrayList<>(), new ArrayList<>(), 
            new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), 
            new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

        assertThat(pc.codeProduit()).isEqualTo("PROD002");
        assertThat(pc.modeCalcul()).isEqualTo("CUSTOM");
        assertThat(pc.dateEffetDebut()).isEqualTo(start);
        assertThat(pc.dateEffetFin()).isEqualTo(end);
    }

    @Test
    void shouldBeEqualWhenSameValues() {
        LocalDate date = LocalDate.of(2023, 1, 1);
        DetailConfig dc = new DetailConfig("ASSURANCE", "ASSOC001", new Reference("TERR001", "France"), 
            new Reference("TERR001", "France"), new Reference("TERR001", "France"), "01/01", "01/01", 
            true, new Reference("MF001", "Monthly"), 0.05, 0.03, 0.02, 0.01, 10.0, 15.0, 0.9, 500000.0, 1.1, 0.95);
        
        ProductConfiguration pc1 = new ProductConfiguration("PROD001", "P001", "Product 1", date, date, 
            "ACTIVE", "STANDARD", "YES", dc, new ArrayList<>(), new ArrayList<>(), 
            new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), 
            new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        ProductConfiguration pc2 = new ProductConfiguration("PROD001", "P001", "Product 1", date, date, 
            "ACTIVE", "STANDARD", "YES", dc, new ArrayList<>(), new ArrayList<>(), 
            new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), 
            new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

        assertThat(pc1).isEqualTo(pc2);
    }

    @Test
    void shouldHandleNullValues() {
        ProductConfiguration pc = new ProductConfiguration(null, null, null, null, null, null, null, 
            null, null, null, null, null, null, null, null, null, null, null, null, null);

        assertThat(pc.codeProduit()).isNull();
        assertThat(pc.code()).isNull();
        assertThat(pc.libelle()).isNull();
    }

    @Test
    void shouldHaveSameHashCodeForEqualObjects() {
        LocalDate date = LocalDate.of(2023, 1, 1);
        DetailConfig dc = new DetailConfig("ASSURANCE", "ASSOC001", new Reference("TERR001", "France"), 
            new Reference("TERR001", "France"), new Reference("TERR001", "France"), "01/01", "01/01", 
            true, new Reference("MF001", "Monthly"), 0.05, 0.03, 0.02, 0.01, 10.0, 15.0, 0.9, 500000.0, 1.1, 0.95);
        
        ProductConfiguration pc1 = new ProductConfiguration("PROD001", "P001", "Product 1", date, date, 
            "ACTIVE", "STANDARD", "YES", dc, new ArrayList<>(), new ArrayList<>(), 
            new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), 
            new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        ProductConfiguration pc2 = new ProductConfiguration("PROD001", "P001", "Product 1", date, date, 
            "ACTIVE", "STANDARD", "YES", dc, new ArrayList<>(), new ArrayList<>(), 
            new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), 
            new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

        assertThat(pc1).hasSameHashCodeAs(pc2);
    }

    @Test
    void shouldContainFieldValuesInToString() {
        LocalDate date = LocalDate.of(2023, 1, 1);
        ProductConfiguration pc = new ProductConfiguration("PROD003", "P003", "Product 3", date, date, 
            "ACTIVE", "STANDARD", "YES", null, new ArrayList<>(), new ArrayList<>(), 
            new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), 
            new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        String toStringResult = pc.toString();

        assertThat(toStringResult).contains("ProductConfiguration");
    }
}
