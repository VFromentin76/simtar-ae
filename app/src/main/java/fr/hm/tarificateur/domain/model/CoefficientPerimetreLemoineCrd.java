package fr.hm.tarificateur.domain.model;

public record CoefficientPerimetreLemoineCrd(
    Integer ageAdhesion,
    String branche,
    Double coefficient
) {
}
