package fr.hm.tarificateur.domain.model;

public record PrimePureCi(
        Integer ageAdhesion,
        Integer dureePretAnnees,
        Double coefficient
) {}

