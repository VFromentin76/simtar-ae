package fr.hm.tarificateur.domain.model;

import java.util.List;

public record ConfigGarantie(
        Reference garantie,
        Boolean booObligatoire,
        Boolean booGafChoisie,
        Integer ageAdhesionMin,
        Integer ageAdhesionMax,
        Integer ageFinCouvertureVie,
        Reference territorialite,
        Double montantMensuelMaxIndemnisableEur,
        Double plafondCapitalMinEur,
        Double plafondCapitalMaxEur,
        List<Dependance> dependances,
        List<FranchiseCoefficient> franchises,
        List<OptionCoefficient> options,
        List<PrimePureCi> primesPuresCi,
        List<PrimePureCrd> primesPuresCrd,
        ExonerationCotisations exonerationCotisations
) {
    public ConfigGarantie(
        Reference garantie,
        Boolean booObligatoire,
        Integer ageAdhesionMin,
        Integer ageAdhesionMax,
        Reference territorialite,
        Double montantMensuelMaxIndemnisableEur,
        Double plafondCapitalMinEur,
        Double plafondCapitalMaxEur,
        List<Dependance> dependances,
        List<FranchiseCoefficient> franchises,
        List<OptionCoefficient> options,
        List<PrimePureCi> primesPuresCi,
        List<PrimePureCrd> primesPuresCrd
    ) {
        this(garantie, booObligatoire, null, ageAdhesionMin, ageAdhesionMax, null,
            territorialite, montantMensuelMaxIndemnisableEur, plafondCapitalMinEur,
            plafondCapitalMaxEur, dependances, franchises, options, primesPuresCi,
            primesPuresCrd, null);
    }
}
