package fr.hm.tarificateur.domain.model;

/**
 * Coefficient tarifaire applicable aux garanties NON_VIE en fonction de la
 * territorialité de souscription (DROM, DROM hors Mayotte, Corse).
 */
public record TerritorialiteCoefficient(
        Reference territorialite,
        Double coefficient
) {}
