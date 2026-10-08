package fr.hm.tarificateur.domain.model;

import java.util.Map;

public record QuotationOptions(
        String bank,
        Boolean couple,
        String effectiveDate,
        Map<String, Loan> loans,
        Integer projectQualification,
        Integer projectState
) {
    public static String mapProjectQualificationCode(Integer projectQualification) {
        if (projectQualification == null) {
            return null;
        }

        return switch (projectQualification) {
            case 1 -> "RESIDENCE_PRINCIPALE";
            case 2 -> "RESIDENCE_SECONDAIRE";
            case 3 -> "INVESTISSEMENT_LOCATIF";
            case 4 -> "TRAVAUX";
            case 5 -> "RACHAT_PRET";
            case 6 -> "AUTRE";
            case 7 -> "PRET_CONSO";
            case 8 -> "RAC_DOMINANTE_CONSOMMATION";
            case 9 -> "RAC_DOMINANTE_IMMOBILIERE";
            case 10 -> "PRET_PERSONNEL";
            case 11 -> "PRET_PROFESSIONNEL_USAGE_HABITATION";
            case 12 -> "CREDIT_BAIL";
            default -> null;
        };
    }
}

