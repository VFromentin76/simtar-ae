package fr.hm.tarificateur.domain.model;

import java.time.LocalDate;
import java.util.List;

public record ProductConfiguration(
        String codeProduit,
        String code,
        String libelle,
        LocalDate dateEffetDebut,
        LocalDate dateEffetFin,
        String statut,
        String modeCalcul,
        String eligibiliteLemoine,
        DetailConfig detailConfig,
        List<ConfigGarantie> garanties,
        List<TypePretEligibilite> typePretEligibilites,
        List<ObjetPretEligibilite> objetPretEligibilites,
        List<TypePretCoefficient> typePretCoefficients,
        List<ObjetPretCoefficient> objetPretCoefficients,
        List<ClasseRisqueCoefficient> classeRisqueCoefficients,
        List<CourbeDeformationCrd> courbeDeformationCrd,
        List<CoefficientPassageFumeurCi> coefficientsPassageFumeurCi,
        List<CoefficientPassageFumeurCrd> coefficientsPassageFumeurCrd,
        List<CategorieProClasseRisqueMapping> mappingsCategoriePro,
        List<ProfessionClasseRisqueMapping> mappingsProfession,
        List<CoverageEndCoefficient> coefficientsFinCouvertureAtCi,
        List<TerritorialiteCoefficient> territorialiteCoefficientsNonVie,
        List<CoefficientPerimetreLemoineCi> coefficientsPerimetreLemoineCi,
        List<TypePretCourbeDeformationCrd> typePretCourbesDeformationCrd,
        List<CoefficientPerimetreLemoineCrd> coefficientsPerimetreLemoineCrd,
        List<CoefficientAerasCi> coefficientsAerasRefusCi,
        List<CoefficientAerasCi> coefficientsAerasExclusionCi,
        List<CoefficientAerasCrd> coefficientsAerasRefusCrd,
        List<CoefficientAerasCrd> coefficientsAerasExclusionCrd,
        List<ExclusionCategoriePro> exclusionsCategoriePro,
        List<java.util.Map<String, Object>> racEligibilites,
        List<java.util.Map<String, Object>> racCoefficients
) {
    public ProductConfiguration(
            String codeProduit, String code, String libelle, LocalDate dateEffetDebut,
            LocalDate dateEffetFin, String statut, String modeCalcul, String eligibiliteLemoine,
            DetailConfig detailConfig, List<ConfigGarantie> garanties,
            List<TypePretEligibilite> typePretEligibilites,
            List<ObjetPretEligibilite> objetPretEligibilites,
            List<TypePretCoefficient> typePretCoefficients,
            List<ObjetPretCoefficient> objetPretCoefficients,
            List<ClasseRisqueCoefficient> classeRisqueCoefficients,
            List<CourbeDeformationCrd> courbeDeformationCrd,
            List<CoefficientPassageFumeurCi> coefficientsPassageFumeurCi,
            List<CoefficientPassageFumeurCrd> coefficientsPassageFumeurCrd,
            List<CategorieProClasseRisqueMapping> mappingsCategoriePro,
            List<ProfessionClasseRisqueMapping> mappingsProfession
    ) {
        this(codeProduit, code, libelle, dateEffetDebut, dateEffetFin, statut, modeCalcul,
            eligibiliteLemoine, detailConfig, garanties, typePretEligibilites,
            objetPretEligibilites, typePretCoefficients, objetPretCoefficients,
            classeRisqueCoefficients, courbeDeformationCrd, coefficientsPassageFumeurCi,
            coefficientsPassageFumeurCrd, mappingsCategoriePro, mappingsProfession, List.of());
    }

    public ProductConfiguration(
            String codeProduit, String code, String libelle, LocalDate dateEffetDebut,
            LocalDate dateEffetFin, String statut, String modeCalcul, String eligibiliteLemoine,
            DetailConfig detailConfig, List<ConfigGarantie> garanties,
            List<TypePretEligibilite> typePretEligibilites,
            List<ObjetPretEligibilite> objetPretEligibilites,
            List<TypePretCoefficient> typePretCoefficients,
            List<ObjetPretCoefficient> objetPretCoefficients,
            List<ClasseRisqueCoefficient> classeRisqueCoefficients,
            List<CourbeDeformationCrd> courbeDeformationCrd,
            List<CoefficientPassageFumeurCi> coefficientsPassageFumeurCi,
            List<CoefficientPassageFumeurCrd> coefficientsPassageFumeurCrd,
            List<CategorieProClasseRisqueMapping> mappingsCategoriePro,
            List<ProfessionClasseRisqueMapping> mappingsProfession,
            List<CoverageEndCoefficient> coefficientsFinCouvertureAtCi
    ) {
        this(codeProduit, code, libelle, dateEffetDebut, dateEffetFin, statut, modeCalcul,
            eligibiliteLemoine, detailConfig, garanties, typePretEligibilites,
            objetPretEligibilites, typePretCoefficients, objetPretCoefficients,
            classeRisqueCoefficients, courbeDeformationCrd, coefficientsPassageFumeurCi,
            coefficientsPassageFumeurCrd, mappingsCategoriePro, mappingsProfession,
            coefficientsFinCouvertureAtCi, List.of());
    }

    public ProductConfiguration(
            String codeProduit, String code, String libelle, LocalDate dateEffetDebut,
            LocalDate dateEffetFin, String statut, String modeCalcul, String eligibiliteLemoine,
            DetailConfig detailConfig, List<ConfigGarantie> garanties,
            List<TypePretEligibilite> typePretEligibilites,
            List<ObjetPretEligibilite> objetPretEligibilites,
            List<TypePretCoefficient> typePretCoefficients,
            List<ObjetPretCoefficient> objetPretCoefficients,
            List<ClasseRisqueCoefficient> classeRisqueCoefficients,
            List<CourbeDeformationCrd> courbeDeformationCrd,
            List<CoefficientPassageFumeurCi> coefficientsPassageFumeurCi,
            List<CoefficientPassageFumeurCrd> coefficientsPassageFumeurCrd,
            List<CategorieProClasseRisqueMapping> mappingsCategoriePro,
            List<ProfessionClasseRisqueMapping> mappingsProfession,
            List<CoverageEndCoefficient> coefficientsFinCouvertureAtCi,
            List<TerritorialiteCoefficient> territorialiteCoefficientsNonVie
    ) {
        this(codeProduit, code, libelle, dateEffetDebut, dateEffetFin, statut, modeCalcul,
            eligibiliteLemoine, detailConfig, garanties, typePretEligibilites,
            objetPretEligibilites, typePretCoefficients, objetPretCoefficients,
            classeRisqueCoefficients, courbeDeformationCrd, coefficientsPassageFumeurCi,
            coefficientsPassageFumeurCrd, mappingsCategoriePro, mappingsProfession,
            coefficientsFinCouvertureAtCi, territorialiteCoefficientsNonVie, List.of());
    }

    public ProductConfiguration(
            String codeProduit, String code, String libelle, LocalDate dateEffetDebut,
            LocalDate dateEffetFin, String statut, String modeCalcul, String eligibiliteLemoine,
            DetailConfig detailConfig, List<ConfigGarantie> garanties,
            List<TypePretEligibilite> typePretEligibilites,
            List<ObjetPretEligibilite> objetPretEligibilites,
            List<TypePretCoefficient> typePretCoefficients,
            List<ObjetPretCoefficient> objetPretCoefficients,
            List<ClasseRisqueCoefficient> classeRisqueCoefficients,
            List<CourbeDeformationCrd> courbeDeformationCrd,
            List<CoefficientPassageFumeurCi> coefficientsPassageFumeurCi,
            List<CoefficientPassageFumeurCrd> coefficientsPassageFumeurCrd,
            List<CategorieProClasseRisqueMapping> mappingsCategoriePro,
            List<ProfessionClasseRisqueMapping> mappingsProfession,
            List<CoverageEndCoefficient> coefficientsFinCouvertureAtCi,
            List<TerritorialiteCoefficient> territorialiteCoefficientsNonVie,
            List<CoefficientPerimetreLemoineCi> coefficientsPerimetreLemoineCi
    ) {
        this(codeProduit, code, libelle, dateEffetDebut, dateEffetFin, statut, modeCalcul,
            eligibiliteLemoine, detailConfig, garanties, typePretEligibilites,
            objetPretEligibilites, typePretCoefficients, objetPretCoefficients,
            classeRisqueCoefficients, courbeDeformationCrd, coefficientsPassageFumeurCi,
            coefficientsPassageFumeurCrd, mappingsCategoriePro, mappingsProfession,
            coefficientsFinCouvertureAtCi, territorialiteCoefficientsNonVie,
            coefficientsPerimetreLemoineCi, List.of(), List.of(), List.of(), List.of(),
            List.of(), List.of(), List.of(), List.of(), List.of());
    }
}
