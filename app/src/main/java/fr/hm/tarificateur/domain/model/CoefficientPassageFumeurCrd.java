package fr.hm.tarificateur.domain.model;

public record CoefficientPassageFumeurCrd(
        Integer ageAtteint,
        String branche,
        Double coefficient
) {}

