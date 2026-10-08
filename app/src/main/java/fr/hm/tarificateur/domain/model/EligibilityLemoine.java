package fr.hm.tarificateur.domain.model;

import java.util.List;

/**
 * Éligibilité et contexte Lemoine - regroupe les paramètres spécifiques au régime Lemoine
 * (éligibilité et coefficients de périmètre).
 *
 * @param eligibilite                      Indique si l'emprunteur est éligible à Lemoine
 * @param coefficientsPerimetreLemoineCi   Coefficients de périmètre Lemoine
 */
public record EligibilityLemoine(
    boolean eligibilite,
    List<CoefficientPerimetreLemoineCi> coefficientsPerimetreLemoineCi
) {}

