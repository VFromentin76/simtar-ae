package fr.hm.tarificateur.domain.model;

public record CoefficientPassageFumeurCi(
        Integer ageAdhesion,
        String branche,
        Double coefficient
) {}

