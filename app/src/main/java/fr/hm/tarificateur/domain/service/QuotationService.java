package fr.hm.tarificateur.domain.service;

import fr.hm.tarificateur.domain.exception.ProductConfigurationNotFoundException;
import fr.hm.tarificateur.domain.exception.QuotationValidationException;
import fr.hm.tarificateur.domain.model.ClassificationRisque;
import fr.hm.tarificateur.domain.model.CoefficientPrimePureCi;
import fr.hm.tarificateur.domain.model.ConfigGarantie;
import fr.hm.tarificateur.domain.model.ContexteCalculPrimePure;
import fr.hm.tarificateur.domain.model.ContexteCoefficients;
import fr.hm.tarificateur.domain.model.ContextePret;
import fr.hm.tarificateur.domain.model.CoverageEndCoefficient;
import fr.hm.tarificateur.domain.model.Customer;
import fr.hm.tarificateur.domain.model.CustomerLoanQuote;
import fr.hm.tarificateur.domain.model.CustomerQuote;
import fr.hm.tarificateur.domain.model.EligibilityLemoine;
import fr.hm.tarificateur.domain.model.Loan;
import fr.hm.tarificateur.domain.model.Premium;
import fr.hm.tarificateur.domain.model.ProductConfiguration;
import fr.hm.tarificateur.domain.model.ProductQuote;
import fr.hm.tarificateur.domain.model.ProfilEmprunteur;
import fr.hm.tarificateur.domain.model.PrimePureCi;
import fr.hm.tarificateur.domain.model.Quotation;
import fr.hm.tarificateur.domain.model.QuotationOptions;
import fr.hm.tarificateur.domain.model.QuotationResult;
import fr.hm.tarificateur.domain.model.QuoteTotals;
import fr.hm.tarificateur.domain.model.ScheduleLine;
import fr.hm.tarificateur.domain.model.Warranty;
import fr.hm.tarificateur.domain.model.WarrantyPricingContext;
import fr.hm.tarificateur.domain.service.support.CoefficientResolutionSupport;
import fr.hm.tarificateur.ports.inbound.QuotationInboundPort;
import fr.hm.tarificateur.ports.outbound.ProductConfiguratorPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class QuotationService implements QuotationInboundPort {

    private static final Logger logger = LoggerFactory.getLogger(QuotationService.class);
    private static final int DEFAULT_LOAN_DURATION_MONTHS = 180;
    private static final Set<Integer> LEMOINE_REAL_ESTATE_PROJECT_QUALIFICATIONS =
        Set.of(1, 2, 3, 4, 5, 9, 11);

    private final ProductConfiguratorPort productConfiguratorPort;
    private final PremiumCalculationService premiumCalculationService;
    private final WarrantyCalculationService warrantyCalculationService;

    public QuotationService(ProductConfiguratorPort productConfiguratorPort) {
        this.productConfiguratorPort = productConfiguratorPort;
        this.premiumCalculationService = new PremiumCalculationService();
        this.warrantyCalculationService = new WarrantyCalculationService();
    }

    @Override
    public List<QuotationResult> calculateQuotation(Quotation quotation) {
        validateQuotationInput(quotation);
        Map<String, ProductConfiguration> productConfigs = productConfiguratorPort.getConfigurations();
        return List.of(
            calculateForProduct(quotation, findConfiguration(productConfigs, "CI")),
            calculateForProduct(quotation, findConfiguration(productConfigs, "CRD"))
        );
    }

    private ProductConfiguration findConfiguration(Map<String, ProductConfiguration> configurations, String mode) {
        if (configurations != null) {
            for (ProductConfiguration configuration : configurations.values()) {
                if (configuration != null && mode.equalsIgnoreCase(configuration.modeCalcul())) {
                    return configuration;
                }
            }
        }
        throw new ProductConfigurationNotFoundException(mode);
    }

    private QuotationResult calculateForProduct(Quotation quotation, ProductConfiguration primaryConfig) {
        String creationDate = quotation.creationDate() != null ? quotation.creationDate() : LocalDate.now().toString();
        String quotationId = "Q-" + creationDate + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        List<CustomerQuote> customerQuotes = new ArrayList<>();
        BigDecimal grandTotalMonthly = BigDecimal.ZERO;
        BigDecimal grandTotalAnnual = BigDecimal.ZERO;
        Map<Integer, BigDecimal[]> scheduleAmounts = new TreeMap<>();

        String calculationMode = primaryConfig != null && primaryConfig.modeCalcul() != null
            ? primaryConfig.modeCalcul()
            : "CI";

        if (quotation.customers() != null && quotation.options() != null && quotation.options().loans() != null) {
            Map<String, Loan> loans = quotation.options().loans();

            for (Map.Entry<String, Customer> entry : quotation.customers().entrySet()) {
                String customerRef = entry.getValue().partnerCustomerRef() != null ?
                    entry.getValue().partnerCustomerRef() : entry.getKey();

                List<CustomerLoanQuote> loanQuotes = new ArrayList<>();
                BigDecimal customerAnnualyTotal = BigDecimal.ZERO;

                String classeRisqueCode = resolveClasseRisqueCode(entry.getValue(), primaryConfig);

                for (Map.Entry<String, Loan> loanEntry : loans.entrySet()) {
                    String loanId = loanEntry.getKey();
                    Loan loan = loanEntry.getValue();

                    double amount = parseDouble(loan.amount(), 0.0);
                    Warranty loanWarranty = loan.warranties() != null && !loan.warranties().isEmpty()
                        ? loan.warranties().values().iterator().next()
                        : null;
                    double primePureDCPTIA = calculerPrimePureDCPTIA(
                        amount,
                        entry.getValue().birthDate(),
                        creationDate,
                        loan,
                        primaryConfig,
                        calculationMode
                    );
                    logger.info("Prime pure DCPTIA calculée pour le prêt {} du client {} : {}", loanId, customerRef, primePureDCPTIA);


                    boolean profilLemoine = isLemoineProfile(
                        entry.getValue(), loan, quotation.options(), creationDate);
                    boolean exonerationCotisationsSouscrite = loanWarranty != null
                        && Boolean.TRUE.equals(loanWarranty.exonerationCotisations());
                    var profilEmprunteur = new ProfilEmprunteur(
                        quotation.options() != null && Boolean.TRUE.equals(quotation.options().couple()),
                        profilLemoine,
                        entry.getValue() != null && Boolean.TRUE.equals(entry.getValue().smoker()),
                        premiumCalculationService.calculateAgeAdhesion(entry.getValue().birthDate(), creationDate)
                    );
                    
                    var contextePret = new ContextePret(
                        amount,
                        loan.type() != null ? Loan.mapTypeCode(loan.type()) : null,
                        quotation.options() != null ? QuotationOptions.mapProjectQualificationCode(quotation.options().projectQualification()) : null
                    );
                    
                    var classificationRisque = new ClassificationRisque(
                        classeRisqueCode,
                        "VIE"
                    );
                    
                    var contexteCoefficients = new ContexteCoefficients(
                        primaryConfig != null ? primaryConfig.typePretCoefficients() : null,
                        primaryConfig != null ? primaryConfig.objetPretCoefficients() : null,
                        primaryConfig != null ? primaryConfig.classeRisqueCoefficients() : null,
                        primaryConfig != null ? primaryConfig.coefficientsPassageFumeurCi() : null
                    );
                    
                    var eligibilityLemoine = new EligibilityLemoine(
                        profilLemoine,
                        primaryConfig != null ? primaryConfig.coefficientsPerimetreLemoineCi() : null
                    );
                    
                    var contexteCalcul = new ContexteCalculPrimePure(
                        primePureDCPTIA,
                        primaryConfig != null ? primaryConfig.detailConfig() : null,
                        profilEmprunteur,
                        contextePret,
                        classificationRisque,
                        contexteCoefficients,
                        eligibilityLemoine,
                        exonerationCotisationsSouscrite
                    );
                    
                    double primePureTotale = premiumCalculationService.calculerPrimePureTotale(contexteCalcul);
                    logger.info("Prime pure totale calculée pour le prêt {} du client {} : {}", loanId, customerRef, primePureTotale);

                    double primeAnnualiseeTTC;
                    if (primaryConfig != null) {
                        primeAnnualiseeTTC = premiumCalculationService.applyChargementsEtTaxe(primePureTotale, primaryConfig.detailConfig());
                    } else {
                        primeAnnualiseeTTC = primePureTotale;
                    }
                    logger.info("Prime annualisée TTC calculée pour le prêt {} du client {} : {}", loanId, customerRef, primeAnnualiseeTTC);

                    // Capital assuré du prêt courant, pondéré par la quotité NON VIE :
                    // utilisé uniquement pour le seuil "gros capital" des garanties non vie
                    // (IP/IPP/IPT/ITP/ITT/DOS/PSY/PE). Contrairement à la quotité vie — dont
                    // la somme sur les co-assurés doit couvrir 100% du montant du prêt — la
                    // quotité non vie s'évalue prêt par prêt, sans contrainte de couverture totale.
                    double loanInsuredCapitalNonVie = computeLoanInsuredCapitalNonVie(loan, amount);
                    WarrantyPricingContext warrantyContext = buildWarrantyPricingContext(
                        primaryConfig,
                        quotation.options(),
                        entry.getValue(),
                        loan,
                        loanInsuredCapitalNonVie,
                        loanWarranty
                    );
                    Map<String, Double> primesPuresDesGaranties = warrantyCalculationService.calculerPrimesPuresDesGaranties(
                        loanWarranty,
                        primaryConfig != null ? primaryConfig.garanties() : null,
                        premiumCalculationService.calculateAgeAdhesion(entry.getValue().birthDate(), creationDate),
                        loanDurationYearsForTariff(loan.duration()),
                        amount,
                        resolveQuotityNonVieFactor(loan),
                        warrantyContext);
                    logger.info("Primes par garantie calculées pour le prêt {} du client {} : {}", loanId, customerRef, primesPuresDesGaranties);

                    Map<String, Double> primesTTCDesGaranties = primesPuresDesGaranties.entrySet().stream()
                        .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            e -> primaryConfig != null
                                ? premiumCalculationService.applyChargementsEtTaxe(e.getValue(), primaryConfig.detailConfig())
                                : e.getValue()
                        ));
                    ensureIppInMapForSchedule(
                        primesTTCDesGaranties,
                        loanWarranty,
                        primaryConfig,
                        premiumCalculationService,
                        warrantyCalculationService,
                        premiumCalculationService.calculateAgeAdhesion(entry.getValue().birthDate(), creationDate),
                        loanDurationYearsForTariff(loan.duration()),
                        amount,
                        resolveQuotityNonVieFactor(loan),
                        warrantyContext
                    );
                    logger.info("Primes TTC par garantie calculées pour le prêt {} du client {} : {}", loanId, customerRef, primesTTCDesGaranties);

                    BigDecimal warrantiesPremiumsTotal = sumAsBigDecimal(primesTTCDesGaranties.values());

                    customerAnnualyTotal = customerAnnualyTotal
                        .add(BigDecimal.valueOf(primeAnnualiseeTTC))
                        .add(warrantiesPremiumsTotal);
                    addScheduleAmounts(
                        scheduleAmounts,
                        quotation.options(),
                        loan,
                        creationDate,
                        entry.getValue().birthDate(),
                        primaryConfig,
                        primeAnnualiseeTTC,
                        primesTTCDesGaranties,
                        loanWarranty
                    );

                    loanQuotes.add(new CustomerLoanQuote(
                        loanId,
                        loan.amount(),
                        loan.rate(),
                        primeAnnualiseeTTC,
                        loan.warranties() != null && !loan.warranties().isEmpty() ?
                            loan.warranties().values().iterator().next() : null
                    ));
                }

                BigDecimal customerAnnualTotal = round(customerAnnualyTotal);
                BigDecimal customerMonthlyTotal = round(customerAnnualyTotal.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP));
                grandTotalMonthly = grandTotalMonthly.add(customerMonthlyTotal);
                grandTotalAnnual = grandTotalAnnual.add(customerAnnualTotal);

                customerQuotes.add(new CustomerQuote(
                    customerRef,
                    "validated",
                    new Premium(grandTotalMonthly.doubleValue(), customerAnnualTotal.doubleValue(), "EUR"),
                    loanQuotes,
                    Collections.emptyList()
                ));
            }
        }

        grandTotalMonthly = round(grandTotalMonthly);
        grandTotalAnnual = round(grandTotalAnnual);

        QuoteTotals totals = new QuoteTotals(grandTotalMonthly.doubleValue(), grandTotalAnnual.doubleValue(), "EUR");

        List<ProductQuote> productQuotes = List.of(new ProductQuote(
            primaryConfig.codeProduit(),
            primaryConfig.libelle() != null ? primaryConfig.libelle() : "Produit " + primaryConfig.codeProduit(),
            new Premium(grandTotalMonthly.doubleValue(), grandTotalAnnual.doubleValue(), "EUR"),
            "available"
        ));

        return new QuotationResult(
            "OK",
            creationDate,
            quotationId,
            customerQuotes,
            productQuotes,
            totals,
            Collections.emptyList(),
            Boolean.parseBoolean(quotation.returnResults() != null ? quotation.returnResults() : "true"),
            buildSchedule(scheduleAmounts, quotation.options(), creationDate)
        );
    }

    private void addScheduleAmounts(
        Map<Integer, BigDecimal[]> amounts,
        QuotationOptions options,
        Loan loan,
        String creationDate,
        String birthDate,
        ProductConfiguration productConfiguration,
        double dcPtia,
        Map<String, Double> warranties,
        Warranty warranty) {
        LocalDate start = resolveStartDate(options, creationDate);
        LocalDate loanEnd = start.plusMonths(parseLoanDurationMonths(loan.duration()));
        String ageFinCouvertureOption = warranty != null ? warranty.ageFinCouverture() : null;
        for (int year = start.getYear(); year <= loanEnd.getYear(); year++) {
            addAnnualScheduleAmounts(
                amounts, year, start, loanEnd, creationDate, birthDate, productConfiguration, dcPtia, warranties,
                ageFinCouvertureOption);
        }
    }

    private void addAnnualScheduleAmounts(
        Map<Integer, BigDecimal[]> amounts, int year, LocalDate start, LocalDate loanEnd, String creationDate, String birthDate,
        ProductConfiguration productConfiguration, double dcPtia, Map<String, Double> warranties,
        String ageFinCouvertureOption) {
        double loanFactor = yearFactor(start, loanEnd, year);
        if (loanFactor == 0) {
            return;
        }
        BigDecimal[] values = amounts.computeIfAbsent(year, ignored -> createScheduleAmounts());
        values[0] = values[0].add(BigDecimal.valueOf(dcPtia).multiply(BigDecimal.valueOf(loanFactor)));
        for (Map.Entry<String, Double> warranty : warranties.entrySet()) {
            BigDecimal premium = BigDecimal.valueOf(warranty.getValue()).multiply(BigDecimal.valueOf(warrantyFactor(
                start, loanEnd, year, creationDate, birthDate, productConfiguration, warranty.getKey(),
                ageFinCouvertureOption)));
            addWarrantyToSchedule(values, warranty.getKey(), premium);
        }
        values[9] = values[9]
            .add(BigDecimal.valueOf(dcPtia).multiply(BigDecimal.valueOf(loanFactor)))
            .add(warranties.entrySet().stream()
                .map(warranty -> BigDecimal.valueOf(warranty.getValue()).multiply(BigDecimal.valueOf(warrantyFactor(
                    start, loanEnd, year, creationDate, birthDate, productConfiguration, warranty.getKey(),
                    ageFinCouvertureOption))))
                .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    private void addWarrantyToSchedule(BigDecimal[] values, String code, BigDecimal premium) {
        switch (code) {
            case "ITT" -> values[1] = values[1].add(premium);
            case "IPT" -> values[2] = values[2].add(premium);
            case "IPP" -> values[3] = values[3].add(premium);
            case "IP" -> values[4] = values[4].add(premium);
            case "ITP" -> values[5] = values[5].add(premium);
            case "DOS" -> values[6] = values[6].add(premium);
            case "PSY" -> values[7] = values[7].add(premium);
            case "PE" -> values[8] = values[8].add(premium);
        }
    }

    private double warrantyFactor(
        LocalDate start, LocalDate loanEnd, int year, String creationDate, String birthDate,
        ProductConfiguration productConfiguration, String warrantyCode, String ageFinCouvertureOption) {
        LocalDate coverageEnd = coverageEndDate(
            loanEnd, creationDate, birthDate, productConfiguration, warrantyCode, ageFinCouvertureOption);
        return yearFactor(start, coverageEnd, year);
    }

    private LocalDate coverageEndDate(
        LocalDate loanEnd, String creationDate, String birthDate, ProductConfiguration productConfiguration,
        String warrantyCode, String ageFinCouvertureOption) {
        Integer ageLimit = coverageEndAge(productConfiguration, birthDate, creationDate, ageFinCouvertureOption);
        if (!isWorkIncapacityWarranty(warrantyCode) || ageLimit == null || birthDate == null) {
            return loanEnd;
        }
        LocalDate birthday = LocalDate.parse(birthDate).plusYears(ageLimit);
        LocalDate ageLimitEnd = birthday.withMonth(12).withDayOfMonth(31).plusDays(1);
        return ageLimitEnd.isBefore(loanEnd) ? ageLimitEnd : loanEnd;
    }

    /**
     * Résout l'âge de fin de couverture applicable.
     * <p>
     * Si l'assuré a explicitement retenu une option (65/67/70 ans), seules les
     * lignes de configuration correspondant à ce choix sont considérées. À
     * défaut de choix explicite, on conserve le comportement historique :
     * sélection de la ligne dont l'âge d'adhésion est le plus proche, tous
     * âges de fin de couverture confondus.
     */
    private Integer coverageEndAge(
        ProductConfiguration productConfiguration, String birthDate, String creationDate,
        String ageFinCouvertureOption) {
        if (productConfiguration == null || productConfiguration.coefficientsFinCouvertureAtCi() == null
            || productConfiguration.coefficientsFinCouvertureAtCi().isEmpty() || birthDate == null) {
            return null;
        }
        int ageAtSubscription = premiumCalculationService.calculateAgeAdhesion(
            birthDate, creationDate);
        Integer requestedAgeFinCouverture = parseAgeFinCouvertureOption(ageFinCouvertureOption);
        return productConfiguration.coefficientsFinCouvertureAtCi().stream()
            .filter(rule -> rule.ageAdhesion() != null && rule.ageFinCouverture() != null)
            .filter(rule -> requestedAgeFinCouverture == null || requestedAgeFinCouverture.equals(rule.ageFinCouverture()))
            .min(Comparator.comparingInt(rule -> Math.abs(rule.ageAdhesion() - ageAtSubscription)))
            .map(CoverageEndCoefficient::ageFinCouverture)
            .orElse(null);
    }

    /**
     * Convertit le code d'option API ("AGE_65"/"AGE_67"/"AGE_70") en âge entier.
     * Retourne {@code null} si l'option n'est pas renseignée ou non reconnue.
     */
    static Integer parseAgeFinCouvertureOption(String ageFinCouvertureOption) {
        if (ageFinCouvertureOption == null) {
            return null;
        }
        String digits = ageFinCouvertureOption.replaceAll("\\D+", "");
        return digits.isEmpty() ? null : Integer.valueOf(digits);
    }

    private boolean isWorkIncapacityWarranty(String code) {
        return Set.of("IP", "IPP", "IPT", "ITP", "ITT", "DOS", "PSY", "PE").contains(code);
    }

    private List<ScheduleLine> buildSchedule(
        Map<Integer, BigDecimal[]> amounts,
        QuotationOptions options,
        String creationDate) {
        LocalDate start = resolveStartDate(options, creationDate);
        return amounts.entrySet().stream()
            .map(entry -> new ScheduleLine(
                entry.getKey(),
                round(entry.getValue()[0]).doubleValue(),
                round(entry.getValue()[1]).doubleValue(),
                round(entry.getValue()[2]).doubleValue(),
                round(entry.getValue()[3]).doubleValue(),
                round(entry.getValue()[4]).doubleValue(),
                round(entry.getValue()[5]).doubleValue(),
                round(entry.getValue()[6]).doubleValue(),
                round(entry.getValue()[7]).doubleValue(),
                round(entry.getValue()[8]).doubleValue(),
                round(entry.getValue()[9]).doubleValue(),
                scheduleComment(start, entry.getKey())
            ))
            .toList();
    }

    private LocalDate resolveStartDate(QuotationOptions options, String fallback) {
        return options != null && options.effectiveDate() != null
            ? LocalDate.parse(options.effectiveDate())
            : LocalDate.parse(fallback);
    }

    private double yearFactor(LocalDate start, LocalDate end, int year) {
        LocalDate yearStart = LocalDate.of(year, 1, 1);
        LocalDate yearEnd = yearStart.plusYears(1);
        LocalDate overlapStart = start.isAfter(yearStart) ? start : yearStart;
        LocalDate overlapEnd = end.isBefore(yearEnd) ? end : yearEnd;
        long days = java.time.temporal.ChronoUnit.DAYS.between(overlapStart, overlapEnd);
        return Math.max(0, (double) days / java.time.temporal.ChronoUnit.DAYS.between(yearStart, yearEnd));
    }

    private String scheduleComment(LocalDate start, int year) {
        if (year == start.getYear() && start.getDayOfYear() != 1) {
            return "effet prorata date d'effet";
        }
        return null;
    }

    private List<CoefficientPrimePureCi> resolveCiCoefficients(ProductConfiguration productConfig) {
        if (productConfig == null || productConfig.garanties() == null || productConfig.garanties().isEmpty()) {
            return Collections.emptyList();
        }

        List<CoefficientPrimePureCi> coefficients = new ArrayList<>();
        for (ConfigGarantie garantie : productConfig.garanties()) {
            if (garantie != null && garantie.primesPuresCi() != null && !garantie.primesPuresCi().isEmpty()) {
                for (PrimePureCi primePureCi : garantie.primesPuresCi()) {
                    if (primePureCi != null
                        && primePureCi.ageAdhesion() != null
                        && primePureCi.dureePretAnnees() != null
                        && primePureCi.coefficient() != null) {
                        coefficients.add(new CoefficientPrimePureCi(
                            primePureCi.ageAdhesion(),
                            primePureCi.dureePretAnnees(),
                            primePureCi.coefficient()
                        ));
                    }
                }
            }
        }

        return coefficients;
    }

    private void validateQuotationInput(Quotation quotation) {
        if (quotation == null) {
            throw new QuotationValidationException("La requÃªte de cotation ne peut pas Ãªtre nulle");
        }
        if (quotation.customers() == null || quotation.customers().isEmpty()) {
            throw new QuotationValidationException("Ã‰chec de la validation de la cotation : la liste des clients ne peut pas Ãªtre vide");
        }
        if (quotation.options() == null) {
            throw new QuotationValidationException("Ã‰chec de la validation de la cotation : les options ne peuvent pas Ãªtre nulles");
        }
        if (quotation.options().loans() == null || quotation.options().loans().isEmpty()) {
            throw new QuotationValidationException("Ã‰chec de la validation de la cotation : la liste des prÃªts ne peut pas Ãªtre vide");
        }
        validateTerritorialityOptions(quotation.options().loans());
        validateIptCapitalOption(quotation.options().loans());
    }

    private void validateTerritorialityOptions(Map<String, Loan> loans) {
        boolean conflictingTerritoriality = loans.values().stream()
            .filter(Objects::nonNull)
            .flatMap(loan -> loan.warranties() == null
                ? Stream.empty() : loan.warranties().values().stream())
            .anyMatch(warranty -> warranty != null
                && Boolean.TRUE.equals(warranty.drom()) && Boolean.TRUE.equals(warranty.corse()));
        if (conflictingTerritoriality) {
            throw new QuotationValidationException(
                "Échec de la validation de la cotation : DROM et Corse ne peuvent pas être sélectionnés ensemble");
        }
    }

    private void validateIptCapitalOption(Map<String, Loan> loans) {
        boolean missingWarranty = loans.values().stream()
            .filter(Objects::nonNull)
            .flatMap(loan -> loan.warranties() == null
                ? Stream.empty() : loan.warranties().values().stream())
            .filter(Objects::nonNull)
            .anyMatch(warranty -> Boolean.TRUE.equals(warranty.iptSortieCapital())
                && (!Boolean.TRUE.equals(warranty.ipt()) || !Boolean.TRUE.equals(warranty.itt())));
        if (missingWarranty) {
            throw new QuotationValidationException(
                "Échec de la validation de la cotation : l'option sortie en capital nécessite les garanties IPT et ITT");
        }
    }

    private double calculatePremiumCI(
        double loanAmount,
        String birthDate,
        String creationDate,
        Loan loan,
        ProductConfiguration productConfig) {

        // Calculer l'âge et la durée
        Integer ageAdhesion = premiumCalculationService.calculateAgeAdhesion(birthDate, creationDate);
        Integer loanDurationYears = loanDurationYearsForTariff(loan.duration());

        double insuredCapital = loanAmount * resolveQuotityVieFactor(loan);
        logger.info("Calcul de la prime CI pour un capital assuré de {} €, âge à l'adhésion {}, durée du prêt {} ans, quotité vie {}",
            insuredCapital, ageAdhesion, loanDurationYears, resolveQuotityVieFactor(loan));

        // Extraire tous les coefficients de prime pure CI depuis les garanties
        List<CoefficientPrimePureCi> ciCoefficients = resolveCiCoefficients(productConfig);
        if (ciCoefficients.isEmpty()) {
            // Fallback legacy pour la prime de base
            double basePremium = calculateLegacyPremium(insuredCapital);

            // Ajouter les primes des garanties si disponibles
            List<ConfigGarantie> garanties = productConfig != null ? productConfig.garanties() : null;
            double warrantyPremiums = calculateAndSumWarrantyPremiums(
                loan.warranties(),
                garanties,
                ageAdhesion,
                loanDurationYears,
                loanAmount
            );

            return round(basePremium + warrantyPremiums);
        }

        double parTarifPrimePureDCPtia = premiumCalculationService.insuredCapitalMultiplierParTarifPrimePureDC_PTIA(
            insuredCapital,
            ageAdhesion,
            loanDurationYears,
            ciCoefficients
        );

        return round(parTarifPrimePureDCPtia);
    }

    /**
     * Capital assuré du prêt courant, pondéré par la quotité NON VIE.
     * <p>
     * Sert uniquement à évaluer le seuil "gros capital" appliqué aux garanties
     * non vie (IP, IPP, IPT, ITP, ITT, DOS, PSY, PE). À la différence de la
     * quotité vie — dont la somme sur les co-assurés doit couvrir 100% du
     * montant du prêt — la quotité non vie s'évalue prêt par prêt, sans
     * contrainte de couverture totale au niveau du contrat.
     */
    private double computeLoanInsuredCapitalNonVie(Loan loan, double loanAmount) {
        if (loan == null) {
            return 0.0;
        }
        return loanAmount * resolveQuotityNonVieFactor(loan);
    }

    private String resolveClasseRisqueCode(Customer customer, ProductConfiguration productConfiguration) {
        if (customer == null || productConfiguration == null) {
            return null;
        }
        String cspCode = Customer.mapProfessionCode(customer.profession());
        return CoefficientResolutionSupport.resolveClasseRisqueCode(cspCode, "VIE",
            productConfiguration.mappingsProfession(), productConfiguration.mappingsCategoriePro());
    }

    private WarrantyPricingContext buildWarrantyPricingContext(
        ProductConfiguration primaryConfig,
        QuotationOptions options,
        Customer customer,
        Loan loan,
        double loanInsuredCapitalNonVie,
        Warranty warranty) {

        Boolean couple = options != null ? options.couple() : null;
        Boolean smoker = customer != null ? customer.smoker() : null;
        Boolean lemoineProfile = customer != null
            ? !Boolean.TRUE.equals(customer.disclosedOverLemoineLimit())
            : null;
        String typePret = loan != null && loan.type() != null ? Loan.mapTypeCode(loan.type()) : null;
        String objetPret = options != null
            ? QuotationOptions.mapProjectQualificationCode(options.projectQualification())
            : null;
        String cspCode = customer != null ? Customer.mapProfessionCode(customer.profession()) : null;
        String franchiseCode = customer != null ? Customer.mapFranchiseCode(customer.franchise()) : null;
        String mnoOption = warranty != null && Boolean.TRUE.equals(warranty.mno())
            ? warranty.mnoOption() : null;
        String ippOption = warranty != null && Boolean.TRUE.equals(warranty.ipp())
            ? warranty.ippOption() : null;

        return new WarrantyPricingContext(
            primaryConfig != null ? primaryConfig.detailConfig() : null,
            couple,
            smoker,
            lemoineProfile,
            typePret,
            objetPret,
            cspCode,
            loanInsuredCapitalNonVie,
            primaryConfig != null ? primaryConfig.typePretCoefficients() : null,
            primaryConfig != null ? primaryConfig.objetPretCoefficients() : null,
            primaryConfig != null ? primaryConfig.classeRisqueCoefficients() : null,
            primaryConfig != null ? primaryConfig.coefficientsPassageFumeurCi() : null,
            primaryConfig != null ? primaryConfig.mappingsCategoriePro() : null,
            primaryConfig != null ? primaryConfig.mappingsProfession() : null,
            franchiseCode,
            mnoOption,
            ippOption,
            warranty != null ? warranty.drom() : null,
            warranty != null ? warranty.corse() : null,
            warranty != null ? warranty.iptSortieCapital() : null,
            warranty != null ? warranty.ageFinCouverture() : null,
            primaryConfig != null ? primaryConfig.coefficientsFinCouvertureAtCi() : null,
            primaryConfig != null ? primaryConfig.territorialiteCoefficientsNonVie() : null,
            warranty != null ? warranty.exonerationCotisations() : null,
            primaryConfig != null ? primaryConfig.coefficientsPerimetreLemoineCi() : null
        );
    }

    private double resolveQuotityVieFactor(Loan loan) {
        return resolveQuotityFactor(loan, Warranty::quotityVie);
    }

    private double resolveQuotityNonVieFactor(Loan loan) {
        return resolveQuotityFactor(loan, Warranty::quotityNonVie);
    }

    private double resolveQuotityFactor(Loan loan, java.util.function.Function<Warranty, String> quotityExtractor) {
        if (loan == null || loan.warranties() == null || loan.warranties().isEmpty()) {
            return 1.0;
        }
        Warranty warranty = loan.warranties().values().iterator().next();
        if (warranty == null) {
            return 1.0;
        }
        String quotity = quotityExtractor.apply(warranty);
        if (quotity == null || quotity.isBlank()) {
            return 1.0;
        }
        return parseDouble(quotity, 100.0) / 100.0;
    }

    /**
     * Calcule et somme les primes de toutes les garanties souscrites.
     *
     * @param warranties      Map des garanties souscrites
     * @param configGaranties Configuration des garanties du produit
     * @param ageAdhesion     Age du client à l'adhÃ©sion
     * @param durationYears   Durée du prêt en années
     * @param loanAmount      Montant du prêt en euros
     * @return Somme des primes des garanties en euros (0.0 si aucune)
     */
    private double calculateAndSumWarrantyPremiums(
        Map<String, Warranty> warranties,
        List<ConfigGarantie> configGaranties,
        Integer ageAdhesion,
        Integer durationYears,
        Double loanAmount) {

        if (warranties == null || warranties.isEmpty() || configGaranties == null || configGaranties.isEmpty()) {
            return 0.0;
        }

        // Prendre la premiÃ¨re Warranty (assumant une seule par prÃªt)
        Warranty warranty = warranties.values().stream().findFirst().orElse(null);
        if (warranty == null) {
            return 0.0;
        }

        // Calculer les primes par garantie
        Map<String, Double> premiumsByWarranty = warrantyCalculationService.calculerPrimesPuresDesGaranties(
            warranty,
            configGaranties,
            ageAdhesion,
            durationYears,
            loanAmount,
            parseDouble(warranty.quotityNonVie(), 100.0) / 100.0);

        // Sommer toutes les primes
        return premiumsByWarranty.values().stream()
            .mapToDouble(Double::doubleValue)
            .sum();
    }


    private double calculatePremiumCRD(
        double loanAmount) {
        return calculateLegacyPremium(loanAmount);
    }

    /**
     * Calcule la prime mensuelle d'un prÃªt en fonction du mode de calcul configurÃ©.
     *
     * <p>Cette mÃ©thode dÃ©termine le mode de calcul (CI ou CRD) et dÃ©lÃ¨gue au service
     * de calcul appropriÃ©. Le mode de calcul est lu depuis la configuration produit
     * et dÃ©termine la mÃ©thode de tarification utilisÃ©e.</p>
     *
     * <p><strong>Modes supportÃ©s:</strong>
     * <ul>
     *   <li><strong>CI</strong> (dÃ©faut): Calcul Initiale basÃ© sur l'Ã¢ge Ã  l'adhÃ©sion
     *       et la durÃ©e du prÃªt</li>
     *   <li><strong>CRD</strong>: Calcul basÃ© sur l'Ã¢ge atteint au moment du calcul</li>
     *   <li><strong>Autre</strong>: Fallback sur la prime de secours (taux forfaitaire)</li>
     * </ul></p>
     *
     * @param loanAmount      Montant du prÃªt en euros (montant assurÃ©)
     * @param birthDate       Date de naissance du client au format "YYYY-MM-DD"
     * @param creationDate    Date de crÃ©ation/adhÃ©sion au format "YYYY-MM-DD"
     * @param loan            Objet contenant les dÃ©tails du prÃªt (durÃ©e, taux, etc.)
     * @param productConfig   Configuration produit avec les paramÃ¨tres de tarification
     *                        (modeCalcul, garanties, coefficients, etc.)
     * @param calculationMode Mode de calcul demandÃ© ("CI", "CRD" ou autre)
     * @return Prime mensuelle calculÃ©e en euros, arrondie Ã  2 dÃ©cimales
     * @throws IllegalArgumentException si les dates sont au format invalide
     * @implNote La comparaison du mode est insensible Ã  la casse.
     * Si le mode n'est pas reconnu, le calcul CI est appliquÃ© par dÃ©faut.
     * @see #calculatePremiumCI(double, String, String, Loan, ProductConfiguration)
     * @see #calculatePremiumCRD(double)
     * @see #calculateLegacyPremium(double)
     * @see PremiumCalculationService#calculateAgeAdhesion(String, String)
     */
    private double calculerPrimePureDCPTIA(
        double loanAmount,
        String birthDate,
        String creationDate,
        Loan loan,
        ProductConfiguration productConfig,
        String calculationMode) {

        if ("CRD".equalsIgnoreCase(calculationMode)) {
            return calculatePremiumCRD(loanAmount);
        }
        return calculatePremiumCI(loanAmount, birthDate, creationDate, loan, productConfig);
    }

    private double calculateLegacyPremium(double loanAmount) {
        double baseRateFactor = 0.0015;
        double annualLoanPremium = loanAmount * baseRateFactor;
        return round(annualLoanPremium / 12.0);
    }

    private boolean isLemoineProfile(
        Customer customer, Loan loan, QuotationOptions options, String creationDate) {
        if (customer == null || customer.birthDate() == null || loan == null
            || loan.duration() == null || options == null || options.projectQualification() == null) {
            return false;
        }
        String loanStartDate = options.effectiveDate() != null ? options.effectiveDate() : creationDate;
        LocalDate loanEndDate = LocalDate.parse(loanStartDate)
            .plusMonths(parseLoanDurationMonths(loan.duration()));
        return !Boolean.TRUE.equals(customer.disclosedOverLemoineLimit())
            && LEMOINE_REAL_ESTATE_PROJECT_QUALIFICATIONS.contains(options.projectQualification())
            && loanEndDate.isBefore(LocalDate.parse(customer.birthDate()).plusYears(60));
    }

    private Integer loanDurationYearsForTariff(String durationStr) {
        return (int) Math.ceil(parseLoanDurationMonths(durationStr) / 12.0);
    }

    private Integer parseLoanDurationMonths(String durationStr) {
        if (durationStr == null || durationStr.isBlank()) {
            return DEFAULT_LOAN_DURATION_MONTHS;
        }
        try {
            return Integer.parseInt(durationStr);
        } catch (NumberFormatException e) {
            return DEFAULT_LOAN_DURATION_MONTHS;
        }
    }

    private double parseDouble(String value, double defaultValue) {
        if (value == null || value.isBlank()) return defaultValue;
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private BigDecimal[] createScheduleAmounts() {
        BigDecimal[] values = new BigDecimal[10];
        Arrays.fill(values, BigDecimal.ZERO);
        return values;
    }

    private BigDecimal sumAsBigDecimal(Collection<Double> values) {
        return values == null ? BigDecimal.ZERO : values.stream()
            .filter(Objects::nonNull)
            .map(BigDecimal::valueOf)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal round(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private double round(double value) {
        return CoefficientResolutionSupport.round(value);
    }

    private void ensureIppInMapForSchedule(
        Map<String, Double> warranties,
        Warranty warranty,
        ProductConfiguration primaryConfig,
        PremiumCalculationService premiumCalculationService,
        WarrantyCalculationService warrantyCalculationService,
        Integer ageAdhesion,
        Integer durationYears,
        Double loanAmount,
        Double resolvedQuotityFactor,
        WarrantyPricingContext warrantyContext) {
        if (warranties == null || warranty == null || primaryConfig == null || primaryConfig.garanties() == null) {
            return;
        }

        boolean needsIpp = Boolean.TRUE.equals(warranty.ipp())
            || Boolean.TRUE.equals(warranty.ipt())
            || Boolean.TRUE.equals(warranty.itt());
        if (!needsIpp) {
            return;
        }

        Map<String, Double> recalculated = warrantyCalculationService.calculerPrimesPuresDesGaranties(
            warranty,
            primaryConfig.garanties(),
            ageAdhesion,
            durationYears,
            loanAmount,
            resolvedQuotityFactor,
            warrantyContext
        );
        if (recalculated != null && recalculated.containsKey("IPP")) {
            double ippPremium = recalculated.get("IPP");
            if (primaryConfig.detailConfig() != null) {
                ippPremium = premiumCalculationService.applyChargementsEtTaxe(
                    ippPremium,
                    primaryConfig.detailConfig()
                );
            }
            warranties.put("IPP", ippPremium);
        }
    }
}
