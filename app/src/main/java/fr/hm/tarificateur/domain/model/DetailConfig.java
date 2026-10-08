package fr.hm.tarificateur.domain.model;

import java.util.List;

public record DetailConfig(
        String typeContrat,
        String associationSouscriptrice,
        Reference territorialiteAdherent,
        Reference territorialiteBienFinance,
        Reference territorialitePrestations,
        String echeanceAnniversaireContrat,
        String echeanceAnniversaireAdhesion,
        Boolean booIndexationGarantiesPrimes,
        Reference modeFractionnement,
        Double tauxTaxEnsembleGaranties,
        Double tauxChargementFraisGestion,
        Double tauxChargementFraisAcquisition,
        Double tauxChargementFraisAssureur,
        Double fraisAssociationEur,
        Double fraisDossierEur,
        Double coefficientExonerationCotisationsEnsembleGaranties,
        Double seuilCoefficientPassageGrosCapital,
        Double coefficientPassageGrosCapital,
        Double coefficientPassageCouple,
        Integer lemoineSeuilAgeTermePret,
        Double lemoineSeuilCapitalAssureEur,
        Double plafondCapitalGarantiesMinEur,
        Double plafondCapitalGarantiesMaxEur,
        List<Reference> territorialitesAdherent,
        List<Reference> territorialitesBienFinance,
        List<Reference> territorialitesPrestations,
        List<Reference> modesFractionnement
) {
    public DetailConfig(
        String typeContrat, String associationSouscriptrice,
        Reference territorialiteAdherent, Reference territorialiteBienFinance,
        Reference territorialitePrestations, String echeanceAnniversaireContrat,
        String echeanceAnniversaireAdhesion, Boolean booIndexationGarantiesPrimes,
        Reference modeFractionnement, Double tauxTaxEnsembleGaranties,
        Double tauxChargementFraisGestion, Double tauxChargementFraisAcquisition,
        Double tauxChargementFraisAssureur, Double fraisAssociationEur,
        Double fraisDossierEur, Double coefficientExonerationCotisationsEnsembleGaranties,
        Double seuilCoefficientPassageGrosCapital, Double coefficientPassageGrosCapital,
        Double coefficientPassageCouple, Integer lemoineSeuilAgeTermePret,
        Double lemoineSeuilCapitalAssureEur, Double plafondCapitalGarantiesMinEur,
        Double plafondCapitalGarantiesMaxEur
    ) {
        this(typeContrat, associationSouscriptrice, territorialiteAdherent,
            territorialiteBienFinance, territorialitePrestations, echeanceAnniversaireContrat,
            echeanceAnniversaireAdhesion, booIndexationGarantiesPrimes, modeFractionnement,
            tauxTaxEnsembleGaranties, tauxChargementFraisGestion, tauxChargementFraisAcquisition,
            tauxChargementFraisAssureur, fraisAssociationEur, fraisDossierEur,
            coefficientExonerationCotisationsEnsembleGaranties, seuilCoefficientPassageGrosCapital,
            coefficientPassageGrosCapital, coefficientPassageCouple, lemoineSeuilAgeTermePret,
            lemoineSeuilCapitalAssureEur, plafondCapitalGarantiesMinEur,
            plafondCapitalGarantiesMaxEur,
            territorialiteAdherent == null ? List.of() : List.of(territorialiteAdherent),
            territorialiteBienFinance == null ? List.of() : List.of(territorialiteBienFinance),
            territorialitePrestations == null ? List.of() : List.of(territorialitePrestations),
            modeFractionnement == null ? List.of() : List.of(modeFractionnement));
    }

    public DetailConfig(
        String typeContrat,
        String associationSouscriptrice,
        Reference territorialiteAdherent,
        Reference territorialiteBienFinance,
        Reference territorialitePrestations,
        String echeanceAnniversaireContrat,
        String echeanceAnniversaireAdhesion,
        Boolean booIndexationGarantiesPrimes,
        Reference modeFractionnement,
        Double tauxTaxEnsembleGaranties,
        Double tauxChargementFraisGestion,
        Double tauxChargementFraisAcquisition,
        Double tauxChargementFraisAssureur,
        Double fraisAssociationEur,
        Double fraisDossierEur,
        Double coefficientExonerationCotisationsEnsembleGaranties,
        Double seuilCoefficientPassageGrosCapital,
        Double coefficientPassageGrosCapital,
        Double coefficientPassageCouple
    ) {
        this(typeContrat, associationSouscriptrice, territorialiteAdherent,
            territorialiteBienFinance, territorialitePrestations, echeanceAnniversaireContrat,
            echeanceAnniversaireAdhesion, booIndexationGarantiesPrimes, modeFractionnement,
            tauxTaxEnsembleGaranties, tauxChargementFraisGestion, tauxChargementFraisAcquisition,
            tauxChargementFraisAssureur, fraisAssociationEur, fraisDossierEur,
            coefficientExonerationCotisationsEnsembleGaranties, seuilCoefficientPassageGrosCapital,
            coefficientPassageGrosCapital, coefficientPassageCouple, null, null, null, null);
    }
}
