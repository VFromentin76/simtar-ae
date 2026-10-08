package fr.hm.tarificateur.domain.model;

public record CoefficientPrimePureCi(
        Integer ageAdhesion,
        Integer dureeEmpruntAnnees,
        Double coefficient
) {}

