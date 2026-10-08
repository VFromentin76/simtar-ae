package fr.hm.tarificateur.domain.model;

public record Beneficiary(
        String birthCity,
        String birthDate,
        String firstname,
        String lastname,
        Integer order
) {}

