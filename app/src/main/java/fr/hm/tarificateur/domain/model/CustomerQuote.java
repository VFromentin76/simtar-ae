package fr.hm.tarificateur.domain.model;

import java.util.List;

public record CustomerQuote(
        String customerRef,
        String status,
        Premium premium,
        List<CustomerLoanQuote> loans,
        List<String> warnings
) {}

