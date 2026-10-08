package fr.hm.tarificateur.domain.model;

public record Customer(
        String partnerCustomerRef,
        String address,
        Beneficiary beneficiaries,
        Integer beneficiaryClause,
        Integer beneficiaryType,
        String city,
        String zipCode,
        String country,
        Integer civility,
        String firstname,
        String lastname,
        String maidenName,
        String birthDate,
        String birthZipcode,
        String birthCity,
        String birthCountry,
        Integer familySituation,
        String cellPhone,
        String homePhone,
        String officePhone,
        String email,
        Integer profession,
        String exactProfession,
        Integer franchise,
        Boolean handling,
        Boolean height,
        Boolean businessTrip,
        Integer isMainCustomer,
        Boolean partTime,
        Boolean riskyProfession,
        Boolean riskySport,
        Integer riskyProfessionId,
        Boolean smoker,
        Boolean disclosedOverLemoineLimit,
        Boolean travelingAbroad,
        Integer modulation,
        Address nextAddress,
        String fees,
        Integer brokerFees,
        Boolean isBrokerFeesSpread
) {
    public static String mapProfessionCode(Integer profession) {
        if (profession == null) {
            return null;
        }
        return switch (profession) {
            case 1 -> "SANS_ACTIVITE";
            case 2 -> "OUVRIERS";
            case 3 -> "ARTISANS";
            case 4 -> "CHAUFFEURS";
            case 5 -> "COMMERCANTS_CHEFS_ENTREPRISE";
            case 6 -> "EMPLOYES";
            case 7 -> "INGENIEURS_CADRES";
            case 8 -> "POLICIERS_MILITAIRES_SURVEILLANCE";
            case 9 -> "PROFESSIONS_AGRICOLES";
            case 10 -> "PROFESSIONS_SANTE_SOCIAL";
            case 11 -> "PROFESSIONS_ENSEIGNEMENT_SCIENTIFIQUES";
            case 12 -> "PROFESSIONS_INFO_ARTS_SPECTACLES";
            case 13 -> "PROFESSIONS_LIBERALES";
            case 14 -> "TECHNICIENS_AGENTS_MAITRISE";
            default -> null;
        };
    }

    public static String mapFranchiseCode(Integer franchise) {
        if (franchise == null) {
            return null;
        }
        return switch (franchise) {
            case 30 -> "FR_30J";
            case 60 -> "FR_60J";
            case 90 -> "FR_90J";
            case 120 -> "FR_120J";
            case 180 -> "FR_180J";
            default -> null;
        };
    }
}
