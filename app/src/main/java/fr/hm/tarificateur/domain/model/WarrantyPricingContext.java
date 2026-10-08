package fr.hm.tarificateur.domain.model;

import java.util.List;

/**
 * Contexte tarifaire complet transmis au calcul d'une prime de garantie.
 * <p>
 * Regroupe l'ensemble des données nécessaires à l'application des coefficients :
 * CSP, type de prêt, objet de prêt, fumeur, couple, gros capitaux, exonération,
 * Lemoine.
 * <p>
 * Le seuil "gros capitaux" est évalué au niveau du prêt courant via
 * {@code loanInsuredCapitalNonVie} (capital assuré du prêt, pondéré par la
 * quotité NON VIE). Ce champ ne concerne que les garanties non vie (IP, IPP,
 * IPT, ITP, ITT, DOS, PSY, PE) : contrairement à la quotité vie — dont la
 * somme sur les co-assurés doit couvrir 100% du montant du prêt — la quotité
 * non vie s'évalue prêt par prêt, sans contrainte de couverture totale au
 * niveau du contrat.
 * <p>
 * Toute donnée manquante ({@code null}) implique un coefficient neutre (1.0)
 * pour la dimension concernée.
 */
public record WarrantyPricingContext(
        DetailConfig detailConfig,
        Boolean couple,
        Boolean smoker,
        Boolean lemoineProfile,
        String typePret,
        String objetPret,
        String cspCode,
        Double loanInsuredCapitalNonVie,
        List<TypePretCoefficient> typePretCoefficients,
        List<ObjetPretCoefficient> objetPretCoefficients,
        List<ClasseRisqueCoefficient> classeRisqueCoefficients,
        List<CoefficientPassageFumeurCi> coefficientsPassageFumeurCi,
        List<CategorieProClasseRisqueMapping> mappingsCategoriePro,
        List<ProfessionClasseRisqueMapping> mappingsProfession,
        String franchiseCode,
        String mnoOption,
        String ippOption,
        Boolean drom,
        Boolean corse,
        Boolean iptSortieCapital,
        String ageFinCouvertureOption,
        List<CoverageEndCoefficient> coverageEndCoefficients,
        List<TerritorialiteCoefficient> territorialiteCoefficientsNonVie,
        Boolean exonerationCotisations,
        List<CoefficientPerimetreLemoineCi> coefficientsPerimetreLemoineCi
) {
    public WarrantyPricingContext(
        DetailConfig detailConfig, Boolean couple, Boolean smoker, Boolean lemoineProfile,
        String typePret, String objetPret, String cspCode, Double loanInsuredCapitalNonVie,
        List<TypePretCoefficient> typePretCoefficients, List<ObjetPretCoefficient> objetPretCoefficients,
        List<ClasseRisqueCoefficient> classeRisqueCoefficients,
        List<CoefficientPassageFumeurCi> coefficientsPassageFumeurCi,
        List<CategorieProClasseRisqueMapping> mappingsCategoriePro,
        List<ProfessionClasseRisqueMapping> mappingsProfession, String franchiseCode) {
        this(detailConfig, couple, smoker, lemoineProfile, typePret, objetPret, cspCode,
                loanInsuredCapitalNonVie, typePretCoefficients, objetPretCoefficients,
                classeRisqueCoefficients, coefficientsPassageFumeurCi, mappingsCategoriePro,
                mappingsProfession, franchiseCode, null, null,
                null, null, null, null, null, null, null, null);
    }

    public WarrantyPricingContext(
        DetailConfig detailConfig, Boolean couple, Boolean smoker, Boolean lemoineProfile,
        String typePret, String objetPret, String cspCode, Double loanInsuredCapitalNonVie,
        List<TypePretCoefficient> typePretCoefficients, List<ObjetPretCoefficient> objetPretCoefficients,
        List<ClasseRisqueCoefficient> classeRisqueCoefficients,
        List<CoefficientPassageFumeurCi> coefficientsPassageFumeurCi,
        List<CategorieProClasseRisqueMapping> mappingsCategoriePro,
        List<ProfessionClasseRisqueMapping> mappingsProfession, String franchiseCode, String mnoOption) {
        this(detailConfig, couple, smoker, lemoineProfile, typePret, objetPret, cspCode,
                loanInsuredCapitalNonVie, typePretCoefficients, objetPretCoefficients,
                classeRisqueCoefficients, coefficientsPassageFumeurCi, mappingsCategoriePro,
                mappingsProfession, franchiseCode, mnoOption, null,
                null, null, null, null, null, null, null, null);
    }

    public WarrantyPricingContext(
        DetailConfig detailConfig, Boolean couple, Boolean smoker, Boolean lemoineProfile,
        String typePret, String objetPret, String cspCode, Double loanInsuredCapitalNonVie,
        List<TypePretCoefficient> typePretCoefficients, List<ObjetPretCoefficient> objetPretCoefficients,
        List<ClasseRisqueCoefficient> classeRisqueCoefficients,
        List<CoefficientPassageFumeurCi> coefficientsPassageFumeurCi,
        List<CategorieProClasseRisqueMapping> mappingsCategoriePro,
        List<ProfessionClasseRisqueMapping> mappingsProfession, String franchiseCode,
        String mnoOption, Boolean drom, Boolean corse, Boolean iptSortieCapital,
        String ageFinCouvertureOption,
        List<CoverageEndCoefficient> coverageEndCoefficients,
        List<TerritorialiteCoefficient> territorialiteCoefficientsNonVie) {
        this(detailConfig, couple, smoker, lemoineProfile, typePret, objetPret, cspCode,
                loanInsuredCapitalNonVie, typePretCoefficients, objetPretCoefficients,
                classeRisqueCoefficients, coefficientsPassageFumeurCi, mappingsCategoriePro,
                mappingsProfession, franchiseCode, mnoOption, null,
                drom, corse, iptSortieCapital, ageFinCouvertureOption,
                coverageEndCoefficients, territorialiteCoefficientsNonVie,
                null, null);
    }

    public WarrantyPricingContext(
        DetailConfig detailConfig, Boolean couple, Boolean smoker, Boolean lemoineProfile,
        String typePret, String objetPret, String cspCode, Double loanInsuredCapitalNonVie,
        List<TypePretCoefficient> typePretCoefficients, List<ObjetPretCoefficient> objetPretCoefficients,
        List<ClasseRisqueCoefficient> classeRisqueCoefficients,
        List<CoefficientPassageFumeurCi> coefficientsPassageFumeurCi,
        List<CategorieProClasseRisqueMapping> mappingsCategoriePro,
        List<ProfessionClasseRisqueMapping> mappingsProfession, String franchiseCode,
        String mnoOption, Boolean drom, Boolean corse, Boolean iptSortieCapital,
        String ageFinCouvertureOption,
        List<CoverageEndCoefficient> coverageEndCoefficients,
        List<TerritorialiteCoefficient> territorialiteCoefficientsNonVie,
        Boolean exonerationCotisations) {
        this(detailConfig, couple, smoker, lemoineProfile, typePret, objetPret, cspCode,
                loanInsuredCapitalNonVie, typePretCoefficients, objetPretCoefficients,
                classeRisqueCoefficients, coefficientsPassageFumeurCi, mappingsCategoriePro,
                mappingsProfession, franchiseCode, mnoOption, null,
                drom, corse, iptSortieCapital, ageFinCouvertureOption,
                coverageEndCoefficients, territorialiteCoefficientsNonVie,
                exonerationCotisations, null);
    }

    public WarrantyPricingContext(
        DetailConfig detailConfig, Boolean couple, Boolean smoker, Boolean lemoineProfile,
        String typePret, String objetPret, String cspCode, Double loanInsuredCapitalNonVie,
        List<TypePretCoefficient> typePretCoefficients, List<ObjetPretCoefficient> objetPretCoefficients,
        List<ClasseRisqueCoefficient> classeRisqueCoefficients,
        List<CoefficientPassageFumeurCi> coefficientsPassageFumeurCi,
        List<CategorieProClasseRisqueMapping> mappingsCategoriePro,
        List<ProfessionClasseRisqueMapping> mappingsProfession, String franchiseCode,
        String mnoOption, String ippOption,
        Boolean drom, Boolean corse, Boolean iptSortieCapital, String ageFinCouvertureOption,
        List<CoverageEndCoefficient> coverageEndCoefficients,
        List<TerritorialiteCoefficient> territorialiteCoefficientsNonVie,
        Boolean exonerationCotisations) {
        this(detailConfig, couple, smoker, lemoineProfile, typePret, objetPret, cspCode,
                loanInsuredCapitalNonVie, typePretCoefficients, objetPretCoefficients,
                classeRisqueCoefficients, coefficientsPassageFumeurCi, mappingsCategoriePro,
                mappingsProfession, franchiseCode, mnoOption, ippOption,
                drom, corse, iptSortieCapital, ageFinCouvertureOption,
                coverageEndCoefficients, territorialiteCoefficientsNonVie,
                exonerationCotisations, null);
    }

    public static WarrantyPricingContext empty() {
        return new WarrantyPricingContext(
                null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null
        );
    }
}
