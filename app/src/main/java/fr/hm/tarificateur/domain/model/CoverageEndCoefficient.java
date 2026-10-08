package fr.hm.tarificateur.domain.model;

public record CoverageEndCoefficient(
        Integer ageAdhesion,
        Integer ageFinCouverture,
        Double coefficient
) {}
