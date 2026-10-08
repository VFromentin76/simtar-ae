package fr.hm.tarificateur.domain.model;

import java.util.Map;

public record Quotation(
        String creationDate,
        Map<String, Customer> customers,
        QuotationOptions options,
        String returnResults,
        String source,
        Integer paymentFrequency
) {}
