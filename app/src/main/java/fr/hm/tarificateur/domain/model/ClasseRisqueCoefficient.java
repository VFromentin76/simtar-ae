package fr.hm.tarificateur.domain.model;

public record ClasseRisqueCoefficient(
        Reference classeRisque,
        String branche,
        Boolean regimeLemoine,
        Double coeffPassage
) {}

