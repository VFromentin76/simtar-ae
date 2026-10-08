package fr.hm.tarificateur.domain.model;

/**
 * Coefficient de périmètre Lemoine applicable à la prime pure CI, en
 * fonction de l'âge d'adhésion et de la branche (VIE / NON_VIE).
 */
public record CoefficientPerimetreLemoineCi(
        Integer ageAdhesion,
        String branche,
        Double coefficient
) {}
