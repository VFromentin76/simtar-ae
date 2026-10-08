package fr.hm.tarificateur.domain.model;

public record ProductQuote(
        String code,
        String name,
        Premium premium,
        String status
) {}

