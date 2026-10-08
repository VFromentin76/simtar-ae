package fr.hm.tarificateur.domain.model;

public record CoefficientAerasCi(
    Integer ageAdhesion,
    Integer dureePretAnnees,
    Double coefficient
) {
}
