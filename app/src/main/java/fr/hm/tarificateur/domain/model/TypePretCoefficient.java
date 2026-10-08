package fr.hm.tarificateur.domain.model;

public record TypePretCoefficient(
        Reference typePret,
        String branche,
        Boolean regimeLemoine,
        Double coefficient
) {}

