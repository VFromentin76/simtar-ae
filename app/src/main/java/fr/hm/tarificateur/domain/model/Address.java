package fr.hm.tarificateur.domain.model;

public record Address(
        String address,
        String city,
        String moveDate,
        String zipCode
) {}

