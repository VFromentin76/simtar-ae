package fr.hm.tarificateur.domain.model;

import java.util.Map;

public record Loan(
        String amount,
        String duration,
        String delayType,
        String delay,
        Integer partnerLoanRef,
        String rate,
        Integer rateType,
        Integer type,
        Map<String, Warranty> warranties
) {
    public static String mapTypeCode(Integer type) {
        if (type == null) {
            return null;
        }

        return switch (type) {
            case 1 -> "PRET_AMORTISSABLE";
            case 2 -> "PRET_CONSTANT";
            case 3 -> "PRET_PALLIER";
            case 4 -> "PRET_RELAIS";
            case 5 -> "PTZ";
            case 6 -> "PRET_IN_FINE";
            default -> null;
        };
    }
}

