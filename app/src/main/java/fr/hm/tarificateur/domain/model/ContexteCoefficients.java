package fr.hm.tarificateur.domain.model;

import java.util.List;

/**
 * Contexte des coefficients - regroupe tous les référentiels de coefficients
 * nécessaires pour le calcul (tarification, type de prêt, objet de prêt, classe de risque, fumeur).
 *
 * @param typePretCoefficients        Coefficients de passage type de prêt
 * @param objetPretCoefficients       Coefficients de passage objet de prêt
 * @param classeRisqueCoefficients    Coefficients de passage classe de risque
 * @param coefficientsPassageFumeurCi Coefficients de passage fumeur
 */
public record ContexteCoefficients(
    List<TypePretCoefficient> typePretCoefficients,
    List<ObjetPretCoefficient> objetPretCoefficients,
    List<ClasseRisqueCoefficient> classeRisqueCoefficients,
    List<CoefficientPassageFumeurCi> coefficientsPassageFumeurCi
) {}

