package fr.hm.tarificateur.domain.model;

public record ObjetPretCoefficient(
        Reference objetPret,
        Boolean regimeLemoine,
        Double coefficient
) {}

