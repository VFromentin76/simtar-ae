package fr.hm.tarificateur.domain.model;

import java.util.List;

public record QuotationResult(
        String status,
        String creationDate,
        String quotationId,
        List<CustomerQuote> customers,
        List<ProductQuote> products,
        QuoteTotals totals,
        List<String> warnings,
        Boolean returnResults,
        java.util.List<ScheduleLine> schedule
) {
    public QuotationResult(
            String status,
            String creationDate,
            String quotationId,
            List<CustomerQuote> customers,
            List<ProductQuote> products,
            QuoteTotals totals,
            List<String> warnings,
            Boolean returnResults
    ) {
        this(status, creationDate, quotationId, customers, products, totals, warnings, returnResults, null);
    }
}
