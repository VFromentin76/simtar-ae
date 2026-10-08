package fr.hm.tarificateur.domain.model;

public record CustomerLoanQuote(
        String loanId,
        String amount,
        String rate,
        Double premium,
        Warranty covers
) {}

