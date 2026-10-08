package fr.hm.tarificateur.domain.model;

public record QuoteTotals(
        Double monthly,
        Double annual,
        String currency
) {}

