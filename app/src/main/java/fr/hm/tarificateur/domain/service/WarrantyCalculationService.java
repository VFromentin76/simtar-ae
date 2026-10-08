package fr.hm.tarificateur.domain.service;

import fr.hm.tarificateur.domain.exception.QuotationValidationException;
import fr.hm.tarificateur.domain.model.CategorieProClasseRisqueMapping;
import fr.hm.tarificateur.domain.model.ClasseRisqueCoefficient;
import fr.hm.tarificateur.domain.model.CoefficientPassageFumeurCi;
import fr.hm.tarificateur.domain.model.CoefficientPerimetreLemoineCi;
import fr.hm.tarificateur.domain.model.ConfigGarantie;
import fr.hm.tarificateur.domain.model.Dependance;
import fr.hm.tarificateur.domain.model.DetailConfig;
import fr.hm.tarificateur.domain.model.FranchiseCoefficient;
import fr.hm.tarificateur.domain.model.ObjetPretCoefficient;
import fr.hm.tarificateur.domain.model.OptionCoefficient;
import fr.hm.tarificateur.domain.model.PrimePureCi;
import fr.hm.tarificateur.domain.model.ProfessionClasseRisqueMapping;
import fr.hm.tarificateur.domain.model.TypePretCoefficient;
import fr.hm.tarificateur.domain.model.TerritorialiteCoefficient;
import fr.hm.tarificateur.domain.model.Warranty;
import fr.hm.tarificateur.domain.model.WarrantyPricingContext;
import fr.hm.tarificateur.domain.service.support.CoefficientResolutionSupport;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Service de calcul des primes d'assurance par garantie.
 * <p>
 * Gère le calcul des primes pour chaque garantie souscrite (IP, ITT, IPP, etc.)
 * en fonction de la configuration produit, de l'âge d'adhésion et de la durée du prêt.
 * <p>
 * Responsabilités:
 * - Vérifier l'éligibilité de chaque garantie souscrite
 * - Rechercher les coefficients de tarification
 * - Calculer les primes individuelles par garantie
 * - Retourner le détail des primes par code de garantie
 */
public class WarrantyCalculationService {

    private static final Logger logger = Logger.getLogger(WarrantyCalculationService.class.getName());
    private static final double ROUNDING_SCALE = 2.0;
    private static final String BRANCHE_NON_VIE = "NON_VIE";

    /**
     * Calcule les primes pures annuelles pour chaque garantie souscrite.
     *
     * @param warranty              Objet Warranty contenant les garanties souscrites (ip, itt, ipp, etc.)
     * @param configGaranties       Liste des configurations produit pour chaque garantie disponible
     * @param ageAdhesion           Âge du client à l'adhésion (en années)
     * @param durationYears         Durée du prêt (en années)
     * @param loanAmount            Montant du prêt assuré (en euros)
     * @param resolvedQuotityFactor Part du capital assuré selon la quotité non vie (1.0 pour 100 %)
     * @return Map<String, Double> clé=code garantie, valeur=prime annuelle en euros
     * Exemple: {"IP": 10.50, "ITT": 14.17, "IPP": 0.00}
     * N'inclut que les garanties souscrites et éligibles
     * @throws IllegalArgumentException si parameters sont invalides (null ou négatifs)
     * @implNote Les codes de garantie matchent les champs de Warranty (ip, itt, ipp, etc.)
     * Les primes non souscrites ne sont pas incluses dans la map
     * Les garanties non éligibles sont ignorées (log warning)
     */
    public Map<String, Double> calculerPrimesPuresDesGaranties(
        Warranty warranty,
        List<ConfigGarantie> configGaranties,
        Integer ageAdhesion,
        Integer durationYears,
        Double loanAmount, Double resolvedQuotityFactor) {
        return calculerPrimesPuresDesGaranties(
            warranty, configGaranties, ageAdhesion, durationYears,
            loanAmount, resolvedQuotityFactor, WarrantyPricingContext.empty());
    }

    /**
     * Variante enrichie appliquant la chaîne complète de coefficients :
     * base × CSP × typePret × objetPret × fumeur × couple × grosCapital × exonération
     * × lemoine × franchise × MNO × territorialité × sortie IPT × âge de fin de couverture.
     * <p>
     * Un coefficient absent ou non éligible est ignoré (équivalent à ×1.0).
     */
    public Map<String, Double> calculerPrimesPuresDesGaranties(
        Warranty warranty,
        List<ConfigGarantie> configGaranties,
        Integer ageAdhesion,
        Integer durationYears,
        Double loanAmount,
        Double resolvedQuotityFactor,
        WarrantyPricingContext context) {

        Map<String, Double> warrantyPremiums = new HashMap<>();

        if (warranty == null || configGaranties == null || configGaranties.isEmpty()) {
            return warrantyPremiums;
        }
        WarrantyPricingContext ctx = context != null ? context : WarrantyPricingContext.empty();

        // Cartes des garanties souscrites
        Map<String, Boolean> subscribedWarranties = mapSubscribedWarranties(warranty);
        validateMnoDependencies(warranty, configGaranties, subscribedWarranties);

        boolean ippComputationReady = false;
        boolean ittProcessed = false;
        boolean iptProcessed = false;

        // Pour chaque garantie souscrite
        for (Map.Entry<String, Boolean> entry : subscribedWarranties.entrySet()) {
            String warrantyCode = entry.getKey();
            Boolean isSubscribed = entry.getValue();

            if ("IPP".equals(warrantyCode)) {
                // L'IPP dépend du calcul de l'ITT et de l'IPT, donc on attends qu'ils soient traités.
                // Si le flag n'est pas prêt, on laisse la garantie pour la fin.
                if (!ippComputationReady) {
                    continue;
                }

                ConfigGarantie config = findConfigByCode(warrantyCode, configGaranties);
                if (config == null) {
                    logger.warning(() -> String.format("Garantie %s souscrite mais non trouvée dans la configuration produit", warrantyCode));
                    warrantyPremiums.put(warrantyCode, 0.0);
                    continue;
                }

                String ineligibilityReason = checkEligibility(config, ageAdhesion, subscribedWarranties);
                if (ineligibilityReason != null) {
                    logger.warning(() -> String.format("Garantie %s non éligible: %s", warrantyCode, ineligibilityReason));
                    warrantyPremiums.put(warrantyCode, 0.0);
                    continue;
                }

                warrantyPremiums.put(warrantyCode, calculateIppPremium(config, configGaranties,
                    ageAdhesion, durationYears, loanAmount, resolvedQuotityFactor, ctx));
                continue;
            }

            if (!Boolean.TRUE.equals(isSubscribed)) {
                continue;  // Garantie non souscrite
            }

            // Chercher la config produit pour cette garantie
            ConfigGarantie config = findConfigByCode(warrantyCode, configGaranties);
            if (config == null) {
                logger.warning(() -> String.format("Garantie %s souscrite mais non trouvée dans la configuration produit", warrantyCode));
                if (!"MNO".equals(warrantyCode)) {
                    warrantyPremiums.put(warrantyCode, 0.0);
                }
                continue;
            }

            // Vérifier l'éligibilité
            String ineligibilityReason = checkEligibility(config, ageAdhesion, subscribedWarranties);
            if (ineligibilityReason != null) {
                logger.warning(() -> String.format("Garantie %s non éligible: %s", warrantyCode, ineligibilityReason));
                if (!"MNO".equals(warrantyCode)) {
                    warrantyPremiums.put(warrantyCode, 0.0);
                }
                continue;
            }

            if ("MNO".equals(warrantyCode)) {
                continue;
            }

            // Chercher et calculer la prime
            Double premium = calculateWarrantyPremium(
                config,
                configGaranties,
                warrantyCode,
                ageAdhesion,
                durationYears,
                loanAmount,
                resolvedQuotityFactor,
                ctx);

            warrantyPremiums.put(warrantyCode, premium);
            if ("ITT".equals(warrantyCode)) {
                ittProcessed = true;
            }
            if ("IPT".equals(warrantyCode)) {
                iptProcessed = true;
            }
            if (ittProcessed && iptProcessed) {
                ippComputationReady = true;
            }
        }

        if (subscribedWarranties.containsKey("IPP") && !warrantyPremiums.containsKey("IPP")) {
            // L'IPP ne doit être calculée que si ITT ET IPT ont été traités avec succès
            if (!ittProcessed || !iptProcessed) {
                logger.warning(() -> "Garantie IPP souscrite mais ITT ou IPT n'ont pas été traités; IPP sera définie à 0.0");
                warrantyPremiums.put("IPP", 0.0);
            } else {
                ConfigGarantie ippConfig = findConfigByCode("IPP", configGaranties);
                if (ippConfig == null) {
                    logger.warning(() -> "Garantie IPP souscrite mais non trouvée dans la configuration produit");
                    warrantyPremiums.put("IPP", 0.0);
                } else {
                    String ineligibilityReason = checkEligibility(ippConfig, ageAdhesion, subscribedWarranties);
                    if (ineligibilityReason != null) {
                        logger.warning(() -> String.format("Garantie IPP non éligible: %s", ineligibilityReason));
                        warrantyPremiums.put("IPP", 0.0);
                    } else {
                        warrantyPremiums.put("IPP", calculateIppPremium(
                            ippConfig, configGaranties,
                            ageAdhesion, durationYears, loanAmount, resolvedQuotityFactor, ctx));
                    }
                }
            }
        }

        // IPP est toujours calculée et doit apparaître dans le résultat et l'échéancier.
        // Le champ IPP est déjà exposé par le contrat API ; aucun changement OpenAPI n'est requis.
        return warrantyPremiums;
    }

    private Double calculateIppPremium(
        ConfigGarantie ippConfig,
        List<ConfigGarantie> configGaranties,
        Integer ageAdhesion,
        Integer durationYears,
        Double loanAmount,
        Double resolvedQuotityFactor,
        WarrantyPricingContext ctx) {
        Double ittPremium = calculateGenericWarrantyPremium(configGaranties, "ITT", ageAdhesion, durationYears,
            loanAmount, resolvedQuotityFactor, ctx);
        Double iptPremium = calculateGenericWarrantyPremium(configGaranties, "IPT", ageAdhesion, durationYears,
            loanAmount, resolvedQuotityFactor, ctx);

        if (ippConfig == null || ippConfig.options() == null || ippConfig.options().isEmpty()) {
            return 0.0;
        }

        String ipPOptionCode = ctx.ippOption();
        Double ippBasePremium = ittPremium + iptPremium;
        Double ipPOptionCoefficient = ippConfig.options().stream()
            .filter(option -> option != null && option.option() != null && option.option().code() != null)
            .filter(option -> ipPOptionCode == null || ipPOptionCode.equals(option.option().code()))
            .filter(option -> option.regimeLemoine() == null
                || Boolean.TRUE.equals(option.regimeLemoine()) == Boolean.TRUE.equals(ctx.lemoineProfile()))
            .map(OptionCoefficient::coefficient)
            .filter(java.util.Objects::nonNull)
            .findFirst()
            .orElse(null);

        if (ipPOptionCoefficient == null) {
            return 0.0;
        }

        return CoefficientResolutionSupport.round(ippBasePremium * ((ipPOptionCoefficient / 100.0) - 1));
    }

    private void validateMnoDependencies(
        Warranty warranty, List<ConfigGarantie> configGaranties,
        Map<String, Boolean> subscribedWarranties) {
        if (!Boolean.TRUE.equals(warranty.mno())) {
            return;
        }
        ConfigGarantie mnoConfig = findConfigByCode("MNO", configGaranties);
        if (mnoConfig == null) {
            throw new QuotationValidationException(
                "Échec de la validation de la cotation : la configuration MNO est absente");
        }
        String reason = checkDependencies(mnoConfig, subscribedWarranties);
        if (reason != null) {
            throw new QuotationValidationException(
                "Échec de la validation de la cotation : option MNO non éligible, " + reason);
        }
    }

    /**
     * Crée une map des garanties souscrites à partir de l'objet Warranty.
     *
     * @param warranty Objet Warranty contenant les booléens de souscription
     * @return Map<String, Boolean> code de garantie → souscription (true/false)
     */
    private Map<String, Boolean> mapSubscribedWarranties(Warranty warranty) {
        Map<String, Boolean> map = new LinkedHashMap<>();

        if (warranty.ip() != null) {
            map.put("IP", warranty.ip());
        }
        if (warranty.ipt() != null) {
            map.put("IPT", warranty.ipt());
        }
        if (warranty.itp() != null) {
            map.put("ITP", warranty.itp());
        }
        if (warranty.itt() != null) {
            map.put("ITT", warranty.itt());
        }
        // L'IPP dépend de ITT et IPT ; il est calculé après les autres garanties.
        boolean shouldComputeIpp = Boolean.TRUE.equals(warranty.ipp())
            || Boolean.TRUE.equals(warranty.ipt())
            || Boolean.TRUE.equals(warranty.itt());
        if (shouldComputeIpp) {
            map.put("IPP", true);
        }
        if (warranty.dos() != null) {
            map.put("DOS", warranty.dos());
        }
        if (warranty.psy() != null) {
            map.put("PSY", warranty.psy());
        }
        if (warranty.pe() != null) {
            map.put("PE", warranty.pe());
        }
        if (warranty.mno() != null) {
            map.put("MNO", warranty.mno());
        }

        return map;
    }

    /**
     * Cherche la configuration produit pour un code de garantie donné.
     *
     * @param warrantyCode    Code de la garantie (ex: "ITT", "IP", etc.)
     * @param configGaranties Liste des configurations produit
     * @return ConfigGarantie trouvée, ou null si non trouvée
     */
    private ConfigGarantie findConfigByCode(String warrantyCode, List<ConfigGarantie> configGaranties) {
        return configGaranties.stream()
            .filter(g -> g.garantie() != null && warrantyCode.equals(g.garantie().code()))
            .findFirst()
            .orElse(null);
    }

    /**
     * Vérifie l'éligibilité d'une garantie selon les règles configurées.
     * <p>
     * Checks:
     * 1. Âge d'adhésion dans [min, max]
     * 2. Dépendances satisfaites (ex: ITT nécessite IP)
     *
     * @param config               Configuration produit de la garantie
     * @param ageAdhesion          Âge du client à l'adhésion
     * @param subscribedWarranties Map des garanties souscrites
     * @return Raison d'inéligibilité (String) si non éligible, null si éligible
     */
    private String checkEligibility(
        ConfigGarantie config,
        Integer ageAdhesion,
        Map<String, Boolean> subscribedWarranties) {

        // Vérifier l'âge
        if (config.ageAdhesionMin() != null && ageAdhesion < config.ageAdhesionMin()) {
            return "âge " + ageAdhesion + " < minimum " + config.ageAdhesionMin();
        }

        if (config.ageAdhesionMax() != null && ageAdhesion > config.ageAdhesionMax()) {
            return "âge " + ageAdhesion + " > maximum " + config.ageAdhesionMax();
        }

        return checkDependencies(config, subscribedWarranties);
    }

    private String checkDependencies(
        ConfigGarantie config, Map<String, Boolean> subscribedWarranties) {
        if (config.dependances() != null && !config.dependances().isEmpty()) {
            for (Dependance dep : config.dependances()) {
                if (dep.garantieRequise() != null) {
                    String requiredCode = dep.garantieRequise().code();
                    Boolean isSubscribed = subscribedWarranties.getOrDefault(requiredCode, false);

                    if (!isSubscribed) {
                        return "dépendance non satisfaite: " + requiredCode + " n'est pas souscrite";
                    }
                }
            }
        }

        return null;  // Éligible
    }

    private Double calculateWarrantyPremium(
        ConfigGarantie config,
        List<ConfigGarantie> configGaranties,
        String warrantyCode,
        Integer ageAdhesion,
        Integer durationYears,
        Double loanAmount,
        Double resolvedQuotityFactor,
        WarrantyPricingContext ctx) {

        if (config.primesPuresCi() == null || config.primesPuresCi().isEmpty()) {
            return 0.0;
        }

        PrimePureCi coefficient = findPrimePureCiCoefficient(config, ageAdhesion, durationYears);
        if (coefficient == null) {
            return 0.0;
        }
        double insuredCapital = loanAmount * resolvedQuotityFactor;
        double basePremium = insuredCapital * coefficient.coefficient();

        String branche = resolveBranche(warrantyCode);
        double premium = applyPricingChain(
            config, configGaranties, basePremium, ageAdhesion, branche, warrantyCode, ctx);
        return CoefficientResolutionSupport.round(premium);
    }


    private Double calculateGenericWarrantyPremium(
        List<ConfigGarantie> configGaranties,
        String warrantyCode,
        Integer ageAdhesion,
        Integer durationYears,
        Double loanAmount,
        Double resolvedQuotityFactor,
        WarrantyPricingContext ctx) {
        ConfigGarantie targetConfig = findConfigByCode(warrantyCode, configGaranties);
        if (targetConfig == null || targetConfig.primesPuresCi() == null || targetConfig.primesPuresCi().isEmpty()) {
            return 0.0;
        }

        PrimePureCi coefficient = findPrimePureCiCoefficient(targetConfig, ageAdhesion, durationYears);
        if (coefficient == null) {
            return 0.0;
        }

        double insuredCapital = loanAmount * resolvedQuotityFactor;
        double basePremium = insuredCapital * coefficient.coefficient();
        String branche = resolveBranche(warrantyCode);
        double premium = applyPricingChain(
            targetConfig, configGaranties, basePremium, ageAdhesion, branche, warrantyCode, ctx);
        return CoefficientResolutionSupport.round(premium);
    }

    private PrimePureCi findPrimePureCiCoefficient(
        ConfigGarantie config,
        Integer ageAdhesion,
        Integer durationYears) {
        if (config == null || config.primesPuresCi() == null || config.primesPuresCi().isEmpty()) {
            return null;
        }

        return config.primesPuresCi().stream()
            .filter(p -> p.ageAdhesion().equals(ageAdhesion) && p.dureePretAnnees().equals(durationYears))
            .findFirst()
            .orElseGet(() -> config.primesPuresCi().stream()
                .filter(p -> p.ageAdhesion() != null && p.dureePretAnnees() != null)
                .min(Comparator.comparingDouble(p -> Math.abs(p.ageAdhesion() - ageAdhesion)
                    + Math.abs(p.dureePretAnnees() - durationYears)))
                .orElse(null));
    }

    /**
     * Résout la branche d'assurance associée à un code de garantie.
     * DC/PTIA relèvent de la branche VIE ; les autres garanties (incapacité,
     * invalidité, perte d'emploi, etc.) relèvent de NON_VIE.
     */
    private String resolveBranche(String warrantyCode) {
        if (warrantyCode == null) {
            return null;
        }
        return switch (warrantyCode.toUpperCase()) {
            case "DC", "PTIA" -> "VIE";
            case "IP", "IPP", "IPT", "ITP", "ITT", "DOS", "PSY", "PE" -> BRANCHE_NON_VIE;
            default -> null;
        };
    }

    /**
     * Applique la chaîne de coefficients dans l'ordre exact :
     * base × CSP × typePret × objetPret × fumeur × couple × grosCapital × exonération
     * × lemoine × franchise × MNO × territorialité × sortie IPT × âge de fin de couverture.
     * <p>
     * La franchise est un coefficient spécifique NON_VIE : ignoré si branche != NON_VIE.
     * Un multiplicateur {@code null} est ignoré (équivalent à ×1.0).
     */
    private double applyPricingChain(
        ConfigGarantie config, List<ConfigGarantie> configGaranties,
        double basePremium, Integer ageAdhesion, String branche,
        String warrantyCode, WarrantyPricingContext ctx) {
        double premium = basePremium;
        boolean lemoineProfile = Boolean.TRUE.equals(ctx.lemoineProfile());

        premium = CoefficientResolutionSupport.applyMultiplier(premium, resolveCspMultiplier(ctx, branche, lemoineProfile));
        premium = CoefficientResolutionSupport.applyMultiplier(premium, resolveTypePretMultiplier(ctx, lemoineProfile));
        premium = CoefficientResolutionSupport.applyMultiplier(premium, resolveObjetPretMultiplier(ctx, lemoineProfile));
        premium = CoefficientResolutionSupport.applyMultiplier(premium, resolveFumeurMultiplier(ctx, branche, ageAdhesion));
        premium = CoefficientResolutionSupport.applyMultiplier(premium, resolveCoupleMultiplier(ctx));
        premium = CoefficientResolutionSupport.applyMultiplier(premium, resolveGrosCapitalMultiplier(ctx));
        premium = CoefficientResolutionSupport.applyMultiplier(premium, resolveExonerationMultiplier(ctx));
        premium = CoefficientResolutionSupport.applyMultiplier(premium, resolveLemoineMultiplier(ctx, lemoineProfile, ageAdhesion, branche));
        premium = CoefficientResolutionSupport.applyMultiplier(premium, resolveFranchiseMultiplier(config, ctx, branche, lemoineProfile));
        premium = CoefficientResolutionSupport.applyMultiplier(
            premium, resolveMnoMultiplier(configGaranties, ctx, branche, lemoineProfile));
        premium = CoefficientResolutionSupport.applyMultiplier(premium, resolveTerritorialityMultiplier(ctx, branche));
        premium = CoefficientResolutionSupport.applyMultiplier(
            premium, resolveIptCapitalMultiplier(configGaranties, ctx, warrantyCode, lemoineProfile));
        premium = CoefficientResolutionSupport.applyMultiplier(premium, resolveCoverageEndMultiplier(ctx, branche, ageAdhesion));

        return premium;
    }

    private Double resolveIptCapitalMultiplier(
        List<ConfigGarantie> configGaranties, WarrantyPricingContext ctx,
        String warrantyCode, boolean lemoineProfile) {
        if (!Boolean.TRUE.equals(ctx.iptSortieCapital())
            || (!"IPT".equals(warrantyCode) && !"ITT".equals(warrantyCode))) {
            return null;
        }
        ConfigGarantie iptConfig = findConfigByCode("IPT", configGaranties);
        if (iptConfig == null || iptConfig.options() == null) {
            return null;
        }
        return iptConfig.options().stream()
            .filter(option -> option.option() != null
                && "IPT_SORTIE_CAPITAL".equals(option.option().code()))
            .filter(option -> option.regimeLemoine() != null
                && option.regimeLemoine() == lemoineProfile)
            .map(OptionCoefficient::coefficient)
            .filter(java.util.Objects::nonNull)
            .findFirst()
            .orElse(null);
    }

    /**
     * Résout le coefficient dépendant de l'âge de fin de couverture retenu
     * (65/67/70 ans), en fonction de l'âge à l'adhésion.
     * <p>
     * Ne s'applique qu'aux garanties NON_VIE (incapacité/invalidité) et
     * uniquement si l'assuré a explicitement choisi une option
     * ({@code ctx.ageFinCouvertureOption()}). Sans choix explicite, aucun
     * coefficient n'est appliqué (comportement historique préservé).
     */
    private Double resolveCoverageEndMultiplier(WarrantyPricingContext ctx, String branche, Integer ageAdhesion) {
        if (!BRANCHE_NON_VIE.equals(branche) || ctx.ageFinCouvertureOption() == null
            || ctx.coverageEndCoefficients() == null || ctx.coverageEndCoefficients().isEmpty()
            || ageAdhesion == null) {
            return null;
        }
        Integer requestedAgeFinCouverture = QuotationService.parseAgeFinCouvertureOption(ctx.ageFinCouvertureOption());
        if (requestedAgeFinCouverture == null) {
            return null;
        }
        List<fr.hm.tarificateur.domain.model.CoverageEndCoefficient> matchingRules = ctx.coverageEndCoefficients().stream()
            .filter(rule -> rule.ageAdhesion() != null && rule.coefficient() != null)
            .filter(rule -> requestedAgeFinCouverture.equals(rule.ageFinCouverture()))
            .toList();
        if (matchingRules.isEmpty()) {
            return null;
        }
        Double coefficient = matchingRules.stream()
            .filter(rule -> rule.ageAdhesion().equals(ageAdhesion))
            .map(fr.hm.tarificateur.domain.model.CoverageEndCoefficient::coefficient)
            .findFirst()
            .orElseGet(() -> matchingRules.stream()
                .min(Comparator.comparingInt(rule -> Math.abs(rule.ageAdhesion() - ageAdhesion)))
                .map(fr.hm.tarificateur.domain.model.CoverageEndCoefficient::coefficient)
                .orElse(null));
        // Coefficient stocké en valeur brute (1.0 = neutre) : mis à l'échelle
        // ×100 pour rester cohérent avec applyMultiplier (division par 100).
        return coefficient != null ? coefficient * 100.0 : null;
    }

    /**
     * Résout le coefficient réel de territorialité (DROM/Corse) applicable
     * aux garanties NON_VIE, à partir de {@code territorialiteCoefficientsNonVie}
     * fourni par le configurateur produit.
     * <p>
     * DROM est mappé sur le code "DROM" (Mayotte incluse) sans distinction de
     * Mayotte pour l'instant. Si le coefficient n'est pas trouvé dans la
     * configuration, aucun coefficient n'est appliqué (équivalent à ×1.0).
     */
    private Double resolveTerritorialityMultiplier(WarrantyPricingContext ctx, String branche) {
        if (!BRANCHE_NON_VIE.equals(branche)) {
            return null;
        }
        String territorialiteCode = Boolean.TRUE.equals(ctx.drom()) ? "DROM"
            : Boolean.TRUE.equals(ctx.corse()) ? "CORSE"
            : null;
        if (territorialiteCode == null) {
            return null;
        }
        if (ctx.territorialiteCoefficientsNonVie() == null || ctx.territorialiteCoefficientsNonVie().isEmpty()) {
            return null;
        }
        return ctx.territorialiteCoefficientsNonVie().stream()
            .filter(t -> t.territorialite() != null && territorialiteCode.equals(t.territorialite().code()))
            .map(TerritorialiteCoefficient::coefficient)
            .filter(java.util.Objects::nonNull)
            .findFirst()
            .orElse(null);
    }

    private Double resolveMnoMultiplier(
        List<ConfigGarantie> configGaranties, WarrantyPricingContext ctx,
        String branche, boolean lemoineProfile) {
        if (!BRANCHE_NON_VIE.equals(branche) || ctx.mnoOption() == null) {
            return null;
        }
        ConfigGarantie mnoConfig = findConfigByCode("MNO", configGaranties);
        if (mnoConfig == null || mnoConfig.options() == null) {
            return null;
        }
        return mnoConfig.options().stream()
            .filter(option -> option.option() != null && ctx.mnoOption().equals(option.option().code()))
            .filter(option -> option.regimeLemoine() != null && option.regimeLemoine() == lemoineProfile)
            .map(OptionCoefficient::coefficient)
            .filter(java.util.Objects::nonNull)
            .findFirst()
            .orElse(null);
    }

    /**
     * Résout le coefficient de franchise (garanties NON_VIE uniquement).
     * <p>
     * Recherche dans {@code config.franchises()} l'entrée dont le code correspond
     * à {@code ctx.franchiseCode()} ET dont {@code regimeLemoine} match exactement
     * le profil Lemoine du client. Aucun fallback si le regimeLemoine ne match pas.
     */
    private Double resolveFranchiseMultiplier(ConfigGarantie config, WarrantyPricingContext ctx, String branche, boolean lemoineProfile) {
        if (!BRANCHE_NON_VIE.equals(branche)) {
            return null;
        }
        if (config == null || config.franchises() == null || config.franchises().isEmpty()) {
            return null;
        }
        if (ctx.franchiseCode() == null) {
            return null;
        }
        return config.franchises().stream()
            .filter(f -> f.franchise() != null && ctx.franchiseCode().equals(f.franchise().code()))
            .filter(f -> f.regimeLemoine() != null && f.regimeLemoine() == lemoineProfile)
            .map(FranchiseCoefficient::coefficient)
            .filter(java.util.Objects::nonNull)
            .findFirst()
            .orElse(null);
    }

    private Double resolveCspMultiplier(WarrantyPricingContext ctx, String branche, boolean lemoineProfile) {
        if (ctx.classeRisqueCoefficients() == null || ctx.classeRisqueCoefficients().isEmpty()) {
            return null;
        }
        String classeRisqueCode = resolveClasseRisqueCode(ctx, branche);
        if (classeRisqueCode == null) {
            return null;
        }
        String normalizedBranche = CoefficientResolutionSupport.normalizeBranche(branche);
        return ctx.classeRisqueCoefficients().stream()
            .filter(c -> c != null && c.classeRisque() != null && c.classeRisque().code() != null)
            .filter(c -> classeRisqueCode.equalsIgnoreCase(c.classeRisque().code()))
            .filter(c -> normalizedBranche == null || normalizedBranche.equalsIgnoreCase(CoefficientResolutionSupport.normalizeBranche(c.branche())))
            .filter(c -> c.regimeLemoine() == null || c.regimeLemoine() == lemoineProfile)
            .map(ClasseRisqueCoefficient::coeffPassage)
            .filter(v -> v != null)
            .findFirst()
            .orElse(null);
    }

    private String resolveClasseRisqueCode(WarrantyPricingContext ctx, String branche) {
        return CoefficientResolutionSupport.resolveClasseRisqueCode(
            ctx.cspCode(), branche, ctx.mappingsProfession(), ctx.mappingsCategoriePro());
    }

    private Double resolveTypePretMultiplier(WarrantyPricingContext ctx, boolean lemoineProfile) {
        if (ctx.typePret() == null || ctx.typePretCoefficients() == null || ctx.typePretCoefficients().isEmpty()) {
            return null;
        }
        return ctx.typePretCoefficients().stream()
            .filter(c -> c != null && c.typePret() != null && c.typePret().code() != null)
            .filter(c -> ctx.typePret().equalsIgnoreCase(c.typePret().code()))
            .filter(c -> c.regimeLemoine() == null || c.regimeLemoine() == lemoineProfile)
            .map(TypePretCoefficient::coefficient)
            .filter(v -> v != null)
            .findFirst()
            .orElse(null);
    }

    private Double resolveObjetPretMultiplier(WarrantyPricingContext ctx, boolean lemoineProfile) {
        if (ctx.objetPret() == null || ctx.objetPretCoefficients() == null || ctx.objetPretCoefficients().isEmpty()) {
            return null;
        }
        return ctx.objetPretCoefficients().stream()
            .filter(c -> c != null && c.objetPret() != null && c.objetPret().code() != null)
            .filter(c -> ctx.objetPret().equalsIgnoreCase(c.objetPret().code()))
            .filter(c -> c.regimeLemoine() == null || c.regimeLemoine() == lemoineProfile)
            .map(ObjetPretCoefficient::coefficient)
            .filter(v -> v != null)
            .findFirst()
            .orElse(null);
    }

    private Double resolveFumeurMultiplier(WarrantyPricingContext ctx, String branche, Integer ageAdhesion) {
        if (!Boolean.TRUE.equals(ctx.smoker())) {
            return null;
        }
        return CoefficientResolutionSupport.resolveByAgeAndBranche(
            ctx.coefficientsPassageFumeurCi(), ageAdhesion, branche,
            CoefficientPassageFumeurCi::ageAdhesion, CoefficientPassageFumeurCi::branche,
            CoefficientPassageFumeurCi::coefficient);
    }

    private Double resolveCoupleMultiplier(WarrantyPricingContext ctx) {
        DetailConfig detailConfig = ctx.detailConfig();
        if (!Boolean.TRUE.equals(ctx.couple()) || detailConfig == null
            || detailConfig.coefficientPassageCouple() == null) {
            return null;
        }
        return detailConfig.coefficientPassageCouple();
    }

    /**
     * Seuil "gros capitaux" évalué au niveau du prêt courant : compare le
     * capital assuré non vie du prêt au seuil produit.
     */
    private Double resolveGrosCapitalMultiplier(WarrantyPricingContext ctx) {
        DetailConfig detailConfig = ctx.detailConfig();
        Double loanCapital = ctx.loanInsuredCapitalNonVie();
        if (detailConfig == null || detailConfig.seuilCoefficientPassageGrosCapital() == null
            || detailConfig.coefficientPassageGrosCapital() == null || loanCapital == null) {
            return null;
        }
        if (loanCapital <= detailConfig.seuilCoefficientPassageGrosCapital()) {
            return null;
        }
        return detailConfig.coefficientPassageGrosCapital();
    }

    /**
     * Résout le coefficient d'exonération des cotisations, uniquement si
     * l'assuré a explicitement souscrit cette option
     * ({@code ctx.exonerationCotisations()}). Sans souscription, aucun
     * coefficient n'est appliqué (équivalent à ×1.0).
     */
    private Double resolveExonerationMultiplier(WarrantyPricingContext ctx) {
        DetailConfig detailConfig = ctx.detailConfig();
        if (!Boolean.TRUE.equals(ctx.exonerationCotisations())
            || detailConfig == null || detailConfig.coefficientExonerationCotisationsEnsembleGaranties() == null) {
            return null;
        }
        return detailConfig.coefficientExonerationCotisationsEnsembleGaranties();
    }

    /**
     * Résout le coefficient de périmètre Lemoine applicable, à partir du
     * référentiel {@code ctx.coefficientsPerimetreLemoineCi()} (âge
     * d'adhésion + branche). Recherche une correspondance exacte sur
     * l'âge, puis, à défaut, le coefficient de l'âge le plus proche
     * disponible pour la branche concernée. Retourne {@code null}
     * (équivalent à ×1.0) si le profil n'est pas éligible Lemoine, si
     * l'âge est inconnu, ou si aucun coefficient n'est configuré.
     */
    private Double resolveLemoineMultiplier(
        WarrantyPricingContext ctx, boolean lemoineProfile, Integer ageAdhesion, String branche) {
        if (!lemoineProfile) {
            return null;
        }
        return CoefficientResolutionSupport.resolveByAgeAndBranche(
            ctx.coefficientsPerimetreLemoineCi(), ageAdhesion, branche,
            CoefficientPerimetreLemoineCi::ageAdhesion, CoefficientPerimetreLemoineCi::branche,
            CoefficientPerimetreLemoineCi::coefficient);
    }

    /**
     * Cherche le coefficient le plus proche en termes de distance (Âge, Durée).
     * Utilise une heuristique: minimiser la distance euclidienne.
     *
     * @param coefficients   Liste des coefficients disponibles
     * @param targetAge      Âge cible
     * @param targetDuration Durée cible
     * @return PrimePureCi le plus proche, ou null si liste vide
     */
    private PrimePureCi findClosestCoefficient(
        List<PrimePureCi> coefficients,
        Integer targetAge,
        Integer targetDuration) {

        if (coefficients == null || coefficients.isEmpty()) {
            return null;
        }

        PrimePureCi closest = null;
        double minDistance = Double.MAX_VALUE;

        for (PrimePureCi coeff : coefficients) {
            double distance = Math.sqrt(
                Math.pow((double) coeff.ageAdhesion() - targetAge, 2) +
                    Math.pow((double) coeff.dureePretAnnees() - targetDuration, 2)
            );

            if (distance < minDistance) {
                minDistance = distance;
                closest = coeff;
            }
        }

        return closest;
    }

    /**
     * Arrondit une valeur double à 2 décimales (half-up).
     *
     * @param value Valeur à arrondir
     * @return Valeur arrondie à 2 décimales
     */
    private Double round(Double value) {
        return BigDecimal.valueOf(value)
            .setScale((int) ROUNDING_SCALE, RoundingMode.HALF_UP)
            .doubleValue();
    }
}
