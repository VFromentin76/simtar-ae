package fr.hm.tarificateur.domain.model;

public record OptionCoefficient(
        Reference option,
        Boolean regimeLemoine,
        Double coefficient
) {}

