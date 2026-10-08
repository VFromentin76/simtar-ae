package fr.hm.tarificateur.domain.model;

public record Premium(
        Double monthly,
        Double annual,
        String currency
) {}

