package fr.hm.tarificateur.domain.service;

import fr.hm.tarificateur.domain.model.CoefficientPassageFumeurCi;
import fr.hm.tarificateur.domain.model.CoefficientPerimetreLemoineCi;
import fr.hm.tarificateur.domain.model.CoefficientPrimePureCi;
import fr.hm.tarificateur.domain.model.CoefficientPrimePureCrd;
import fr.hm.tarificateur.domain.model.ClasseRisqueCoefficient;
import fr.hm.tarificateur.domain.model.ContexteCalculPrimePure;
import fr.hm.tarificateur.domain.model.DetailConfig;
import fr.hm.tarificateur.domain.model.ObjetPretCoefficient;
import fr.hm.tarificateur.domain.model.TypePretCoefficient;
import fr.hm.tarificateur.domain.service.support.CoefficientResolutionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;

public class PremiumCalculationService {
    private static final Logger logger = LoggerFactory.getLogger(PremiumCalculationService.class);

    /**
     * Calcule la prime pure annuelle CI (DC/PTIA) sur le capital assuré.
     * <p>
     * Prime annuelle = capital assuré × coefficient CI.
     * <p>
     * La durée du prêt sert à sélectionner le coefficient correspondant à
     * l'âge d'adhésion et à la durée ; elle ne divise pas la prime calculée.
     *
     * @param loanAmount              Capital assuré en euros
     * @param ageAdhesion             Âge du client à la date d'adhésion (pour sélectionner le coefficient)
     * @param loanDurationYears       Durée du prêt en années (pour sélectionner le coefficient)
     * @param coefficientsPrimePureCi Liste des coefficients de tarification CI disponibles
     * @return Prime annuelle CI en euros, arrondie à 2 décimales
     * @throws IllegalArgumentException si aucun coefficient ne peut être trouvé
     */
    public double insuredCapitalMultiplierParTarifPrimePureDC_PTIA(
        Double loanAmount,
        Integer ageAdhesion,
        Integer loanDurationYears,
        List<CoefficientPrimePureCi> coefficientsPrimePureCi) {

        Double primePureCiCoeff = findOrInterpolateCoefficientCI(
            ageAdhesion,
            loanDurationYears,
            coefficientsPrimePureCi
        );

        if (primePureCiCoeff == null) {
            throw new IllegalArgumentException(
                "Impossible de trouver ou interpoler un coefficient CI pour âge=" + ageAdhesion +
                    " et durée=" + loanDurationYears
            );
        }

        return BigDecimal.valueOf(loanAmount)
            .multiply(BigDecimal.valueOf(primePureCiCoeff))
            .setScale(2, RoundingMode.HALF_UP)
            .doubleValue();
    }

    public double calculatePremiumCRD(
        Double loanAmount,
        Integer ageAtteint,
        List<CoefficientPrimePureCrd> coefficientsPrimePureCrd) {

        Double primePureCrdCoeff = findOrInterpolateCoefficientCRD(
            ageAtteint,
            coefficientsPrimePureCrd
        );

        if (primePureCrdCoeff == null) {
            throw new IllegalArgumentException(
                "Impossible de trouver ou interpoler un coefficient CRD pour âge=" + ageAtteint
            );
        }

        return BigDecimal.valueOf(loanAmount)
            .multiply(BigDecimal.valueOf(primePureCrdCoeff))
            .divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP)
            .doubleValue();
    }

    private Double findOrInterpolateCoefficientCI(
        Integer ageAdhesion,
        Integer loanDurationYears,
        List<CoefficientPrimePureCi> coefficients) {

        if (coefficients == null || coefficients.isEmpty()) {
            return null;
        }

        for (CoefficientPrimePureCi coeff : coefficients) {
            if (coeff.ageAdhesion().equals(ageAdhesion) &&
                coeff.dureeEmpruntAnnees().equals(loanDurationYears)) {
                return coeff.coefficient();
            }
        }

        CoefficientPrimePureCi closest = findClosestCoefficientCI(
            ageAdhesion,
            loanDurationYears,
            coefficients
        );

        return closest != null ? closest.coefficient() : null;
    }

    private CoefficientPrimePureCi findClosestCoefficientCI(
        Integer ageAdhesion,
        Integer loanDurationYears,
        List<CoefficientPrimePureCi> coefficients) {

        CoefficientPrimePureCi closest = null;
        double minDistance = Double.MAX_VALUE;

        for (CoefficientPrimePureCi coeff : coefficients) {
            double ageDistance = (double) coeff.ageAdhesion() - ageAdhesion;
            double durationDistance = (double) coeff.dureeEmpruntAnnees() - loanDurationYears;
            double distance = Math.sqrt(
                Math.pow(ageDistance, 2) + Math.pow(durationDistance, 2)
            );

            if (distance < minDistance) {
                minDistance = distance;
                closest = coeff;
            }
        }

        return closest;
    }

    private Double findOrInterpolateCoefficientCRD(
        Integer ageAtteint,
        List<CoefficientPrimePureCrd> coefficients) {

        if (coefficients == null || coefficients.isEmpty()) {
            return null;
        }

        for (CoefficientPrimePureCrd coeff : coefficients) {
            if (coeff.ageAtteint().equals(ageAtteint)) {
                return coeff.coefficient();
            }
        }

        Double interpolated = interpolateCoefficientCRD(ageAtteint, coefficients);
        if (interpolated != null) {
            return interpolated;
        }

        CoefficientPrimePureCrd closest = findClosestCoefficientCRD(ageAtteint, coefficients);
        return closest != null ? closest.coefficient() : null;
    }

    private Double interpolateCoefficientCRD(
        Integer ageAtteint,
        List<CoefficientPrimePureCrd> coefficients) {

        CoefficientPrimePureCrd lower = null;
        CoefficientPrimePureCrd upper = null;

        for (CoefficientPrimePureCrd coeff : coefficients) {
            if (coeff.ageAtteint() < ageAtteint) {
                if (lower == null || coeff.ageAtteint() > lower.ageAtteint()) {
                    lower = coeff;
                }
            } else if (coeff.ageAtteint() > ageAtteint
                && (upper == null || coeff.ageAtteint() < upper.ageAtteint())) {
                upper = coeff;
            }
        }

        if (lower != null && upper != null) {
            int ageDiff = upper.ageAtteint() - lower.ageAtteint();
            double coeffDiff = upper.coefficient() - lower.coefficient();
            int ageFromLower = ageAtteint - lower.ageAtteint();

            return lower.coefficient() + (coeffDiff * ageFromLower / ageDiff);
        }

        return null;
    }

    private CoefficientPrimePureCrd findClosestCoefficientCRD(
        Integer ageAtteint,
        List<CoefficientPrimePureCrd> coefficients) {

        CoefficientPrimePureCrd closest = null;
        int minAgeDiff = Integer.MAX_VALUE;

        for (CoefficientPrimePureCrd coeff : coefficients) {
            int ageDiff = Math.abs(coeff.ageAtteint() - ageAtteint);
            if (ageDiff < minAgeDiff) {
                minAgeDiff = ageDiff;
                closest = coeff;
            }
        }

        return closest;
    }

    public Integer calculateAgeAdhesion(String birthDate, String creationDate) {
        try {
            LocalDate birth = LocalDate.parse(birthDate);
            LocalDate creation = LocalDate.parse(creationDate);
            return Period.between(birth, creation).getYears();
        } catch (Exception e) {
            throw new IllegalArgumentException("Format de date invalide. Attendu: YYYY-MM-DD", e);
        }
    }

    // ...existing code...

    /**
     * Nouvelle signature simplifiée avec contexte thématique.
     * Remplace la signature originale avec 19 paramètres.
     *
     * @param context Contexte complet contenant tous les éléments de calcul
     * @return Prime pure totale en euros, arrondie à 2 décimales
     */
    public double calculerPrimePureTotale(ContexteCalculPrimePure context) {
        double premium = context.premiumBase();

        premium = CoefficientResolutionSupport.applyMultiplier(premium,
            resolveCoupleMultiplier(context.detailConfig(), context.profilEmprunteur().couple()));
        premium = CoefficientResolutionSupport.applyMultiplier(premium,
            resolveExonerationMultiplier(context.detailConfig(), context.exonerationCotisationsSouscrite()));
        premium = CoefficientResolutionSupport.applyMultiplier(premium,
            resolveCapitalMultiplier(context.detailConfig(), context.contextePret().capitalAssure()));
        premium = CoefficientResolutionSupport.applyMultiplier(premium,
            resolveTypePretMultiplier(
                context.contextePret().typePret(),
                context.classificationRisque().branche(),
                context.profilEmprunteur().profilLemoine(),
                context.contexteCoefficients().typePretCoefficients()));
        premium = CoefficientResolutionSupport.applyMultiplier(premium,
            resolveObjetPretMultiplier(
                context.contextePret().objetPret(),
                context.profilEmprunteur().profilLemoine(),
                context.contexteCoefficients().objetPretCoefficients()));
        premium = CoefficientResolutionSupport.applyMultiplier(premium,
            resolveClasseRisqueMultiplier(
                context.classificationRisque().classeRisqueCode(),
                context.classificationRisque().branche(),
                context.profilEmprunteur().profilLemoine(),
                context.contexteCoefficients().classeRisqueCoefficients()));
        premium = CoefficientResolutionSupport.applyMultiplier(premium,
            resolveFumeurMultiplier(
                context.profilEmprunteur().profilFumeur(),
                context.profilEmprunteur().ageAdhesion(),
                context.classificationRisque().branche(),
                context.contexteCoefficients().coefficientsPassageFumeurCi()));

        if (context.eligibilityLemoine().eligibilite()) {
            premium = CoefficientResolutionSupport.applyMultiplier(premium,
                resolveLemoineMultiplier(
                    context.profilEmprunteur().profilLemoine(),
                    context.profilEmprunteur().ageAdhesion(),
                    context.classificationRisque().branche(),
                    context.eligibilityLemoine().coefficientsPerimetreLemoineCi()));
        }

        return CoefficientResolutionSupport.round(premium);
    }

    // ...

    private Double resolveCoupleMultiplier(DetailConfig detailConfig, boolean couple) {
        Double coefficientPassageCouple = null;
        if (!(!couple || detailConfig == null || detailConfig.coefficientPassageCouple() == null)) {
            coefficientPassageCouple = detailConfig.coefficientPassageCouple();
        }
        logger.info("Résolution du coefficient de passage couple : couple={}, coefficientPassageCouple={}", couple, coefficientPassageCouple);
        return coefficientPassageCouple;
    }


    private Double resolveExonerationMultiplier(DetailConfig detailConfig, boolean exonerationCotisationsSouscrite) {

        Double coefficientExonerationCotisationsEnsembleGaranties = null;
        if (exonerationCotisationsSouscrite
            && !(detailConfig == null || detailConfig.coefficientExonerationCotisationsEnsembleGaranties() == null)) {
            coefficientExonerationCotisationsEnsembleGaranties = detailConfig.coefficientExonerationCotisationsEnsembleGaranties();
        }
        logger.info("Résolution du coefficient d'exonération des cotisations pour l'ensemble des garanties : souscrit={}, coefficientExonerationCotisationsEnsembleGaranties={}", exonerationCotisationsSouscrite, coefficientExonerationCotisationsEnsembleGaranties);
        return coefficientExonerationCotisationsEnsembleGaranties;
    }

    private Double resolveCapitalMultiplier(DetailConfig detailConfig, Double capitalAssure) {
        Double coefficientPassageGrosCapital = null;
        if (!(detailConfig == null || detailConfig.seuilCoefficientPassageGrosCapital() == null
            || detailConfig.coefficientPassageGrosCapital() == null || capitalAssure == null)) {
            if (capitalAssure > detailConfig.seuilCoefficientPassageGrosCapital()) {
                coefficientPassageGrosCapital = detailConfig.coefficientPassageGrosCapital();
            }
        }
        logger.info("Résolution du coefficient de passage gros capital : capitalAssure={}, coefficientPassageGrosCapital={}", capitalAssure, coefficientPassageGrosCapital);
        return coefficientPassageGrosCapital;
    }

    private Double resolveTypePretMultiplier(
        String typePret,
        String branche,
        boolean profilLemoine,
        List<TypePretCoefficient> typePretCoefficients) {
        Double typePretMultiplier = null;
        if (!(typePret == null || typePretCoefficients == null || typePretCoefficients.isEmpty())) {
            String normalizedBranche = CoefficientResolutionSupport.normalizeBranche(branche);
            typePretMultiplier = typePretCoefficients.stream()
                .filter(c -> c != null && c.typePret() != null && c.typePret().code() != null)
                .filter(c -> typePret.equalsIgnoreCase(c.typePret().code()))
                .filter(c -> normalizedBranche == null
                    || normalizedBranche.equalsIgnoreCase(CoefficientResolutionSupport.normalizeBranche(c.branche())))
                .filter(c -> c.regimeLemoine() == null || c.regimeLemoine() == profilLemoine)
                .map(TypePretCoefficient::coefficient)
                .filter(coefficient -> coefficient != null)
                .findFirst()
                .orElse(null);
        }
        logger.info("Résolution du coefficient de passage type de prêt : typePret={}, branche={}, profilLemoine={}, coefficient={}",
            typePret, branche, profilLemoine, typePretMultiplier);
        return typePretMultiplier;
    }

    private Double resolveClasseRisqueMultiplier(
        String classeRisqueCode,
        String branche,
        boolean profilLemoine,
        List<ClasseRisqueCoefficient> classeRisqueCoefficients) {
        if (classeRisqueCode == null || classeRisqueCoefficients == null || classeRisqueCoefficients.isEmpty()) {
            return null;
        }
        String normalizedBranche = CoefficientResolutionSupport.normalizeBranche(branche);
        Double coefficient = classeRisqueCoefficients.stream()
            .filter(c -> c != null && c.classeRisque() != null && c.classeRisque().code() != null)
            .filter(c -> classeRisqueCode.equalsIgnoreCase(c.classeRisque().code()))
            .filter(c -> normalizedBranche == null || normalizedBranche.equalsIgnoreCase(CoefficientResolutionSupport.normalizeBranche(c.branche())))
            .filter(c -> c.regimeLemoine() == null || c.regimeLemoine() == profilLemoine)
            .map(ClasseRisqueCoefficient::coeffPassage)
            .filter(coefficientValue -> coefficientValue != null)
            .findFirst()
            .orElse(null);
        logger.info("Résolution du coefficient de passage classe de risque : classeRisqueCode={}, branche={}, profilLemoine={}, coefficient={}",
            classeRisqueCode, branche, profilLemoine, coefficient);
        return coefficient;
    }

    private Double resolveObjetPretMultiplier(String objetPret, boolean profilLemoine, List<ObjetPretCoefficient> objetPretCoefficients) {
        Double objetPretMultiplier = null;
        if (!(objetPret == null || objetPretCoefficients == null || objetPretCoefficients.isEmpty())) {
            objetPretMultiplier = objetPretCoefficients.stream()
                .filter(c -> c != null && c.objetPret() != null && c.objetPret().code() != null)
                .filter(c -> objetPret.equalsIgnoreCase(c.objetPret().code()))
                .filter(c -> c.regimeLemoine() == null || c.regimeLemoine() == profilLemoine)
                .map(ObjetPretCoefficient::coefficient)
                .filter(coefficient -> coefficient != null)
                .findFirst()
                .orElse(null);
        }
        logger.info("Résolution du coefficient de passage objet de prêt : objetPret={}, profilLemoine={}, coefficient={}", objetPret, profilLemoine, objetPretMultiplier);
        return objetPretMultiplier;
    }

    private Double resolveFumeurMultiplier(
        boolean profilFumeur,
        Integer ageAdhesion,
        String branche,
        List<CoefficientPassageFumeurCi> coefficientsPassageFumeurCi) {
        Double fumeurMultiplier = profilFumeur
            ? CoefficientResolutionSupport.resolveByAgeAndBranche(
                coefficientsPassageFumeurCi, ageAdhesion, branche,
                CoefficientPassageFumeurCi::ageAdhesion, CoefficientPassageFumeurCi::branche,
                CoefficientPassageFumeurCi::coefficient)
            : null;

        logger.info("Résolution du coefficient de passage fumeur : profilFumeur={}, ageAdhesion={}, branche={}, coefficient={}", profilFumeur, ageAdhesion, branche, fumeurMultiplier);
        return fumeurMultiplier;
    }

    /**
     * Résout le coefficient de périmètre Lemoine applicable, à partir du
     * référentiel {@code coefficientsPerimetreLemoineCi} (âge d'adhésion +
     * branche). Recherche une correspondance exacte sur l'âge, puis, à
     * défaut, le coefficient de l'âge le plus proche disponible pour la
     * branche concernée. Retourne {@code null} (équivalent à ×1.0) si le
     * profil n'est pas éligible Lemoine, si l'âge est inconnu, ou si aucun
     * coefficient n'est configuré.
     */
    private Double resolveLemoineMultiplier(
        boolean profilLemoine, Integer ageAdhesion, String branche,
        List<CoefficientPerimetreLemoineCi> coefficientsPerimetreLemoineCi) {
        Double coefficient = profilLemoine
            ? CoefficientResolutionSupport.resolveByAgeAndBranche(
                coefficientsPerimetreLemoineCi, ageAdhesion, branche,
                CoefficientPerimetreLemoineCi::ageAdhesion, CoefficientPerimetreLemoineCi::branche,
                CoefficientPerimetreLemoineCi::coefficient)
            : null;

        logger.info("Résolution du coefficient de périmètre Lemoine : profilLemoine={}, ageAdhesion={}, branche={}, coefficient={}",
            profilLemoine, ageAdhesion, branche, coefficient);
        return coefficient;
    }

    public double applyChargementsEtTaxe(double premium, DetailConfig detailConfig) {
        if (detailConfig == null) {
            return premium;
        }

        BigDecimal premiumValue = BigDecimal.valueOf(premium);
        BigDecimal totalCharges = sumCharges(detailConfig);
        if (totalCharges.compareTo(BigDecimal.ZERO) > 0) {
            premiumValue = premiumValue.divide(
                BigDecimal.ONE.subtract(totalCharges.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)),
                10,
                RoundingMode.HALF_UP);
        }

        BigDecimal tax = toBigDecimal(detailConfig.tauxTaxEnsembleGaranties());
        if (tax.compareTo(BigDecimal.ZERO) > 0) {
            premiumValue = premiumValue.multiply(
                BigDecimal.ONE.add(tax.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)));
        }

        return premiumValue.setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private BigDecimal sumCharges(DetailConfig detailConfig) {
        return toBigDecimal(detailConfig.tauxChargementFraisGestion())
            .add(toBigDecimal(detailConfig.tauxChargementFraisAcquisition()))
            .add(toBigDecimal(detailConfig.tauxChargementFraisAssureur()));
    }

    private BigDecimal toBigDecimal(Double value) {
        return value == null ? BigDecimal.ZERO : BigDecimal.valueOf(value);
    }
}
