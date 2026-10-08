package fr.hm.tarificateur.domain.service.support;

import fr.hm.tarificateur.domain.model.CategorieProClasseRisqueMapping;
import fr.hm.tarificateur.domain.model.ProfessionClasseRisqueMapping;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Utilitaires métier partagés entre les services de tarification
 * ({@code PremiumCalculationService}, {@code WarrantyCalculationService},
 * {@code QuotationService}) : normalisation de branche, application de
 * multiplicateur, arrondi monétaire, résolution de coefficient par âge et
 * branche, résolution de la classe de risque à partir de la CSP.
 * <p>
 * Classe utilitaire pure du domaine : aucune annotation Spring, aucune
 * dépendance vers les couches ports/adapters.
 */
public final class CoefficientResolutionSupport {

    private CoefficientResolutionSupport() {
    }

    /**
     * Normalise une branche d'assurance ("vie", " Non-Vie ", etc.) en une
     * forme canonique comparable ("VIE", "NON_VIE").
     */
    public static String normalizeBranche(String branche) {
        if (branche == null) {
            return null;
        }
        return branche.trim().replace('-', '_').toUpperCase();
    }

    /**
     * Applique un multiplicateur exprimé en pourcentage (base 100) à une
     * prime. Un multiplicateur {@code null} est ignoré (équivalent à ×1.0).
     */
    public static double applyMultiplier(double premium, Double multiplier) {
        if (multiplier == null) {
            return premium;
        }
        return premium * multiplier / 100.0;
    }

    /**
     * Arrondit une valeur monétaire à 2 décimales (half-up).
     */
    public static double round(double value) {
        return BigDecimal.valueOf(value)
            .setScale(2, RoundingMode.HALF_UP)
            .doubleValue();
    }

    /**
     * Résout un coefficient dépendant de l'âge d'adhésion et, optionnellement,
     * de la branche, à partir d'un référentiel générique {@code T} (ex :
     * coefficients fumeur, coefficients de périmètre Lemoine).
     * <p>
     * Recherche d'abord une correspondance exacte sur l'âge (et la branche,
     * si celle-ci est renseignée), puis, à défaut, retient le coefficient de
     * l'entrée dont l'âge est le plus proche pour la branche concernée.
     * Retourne {@code null} si aucune entrée n'est disponible ou ne
     * correspond (équivalent à ×1.0).
     *
     * @param coefficients          Référentiel de coefficients à interroger
     * @param ageAdhesion           Âge d'adhésion cible
     * @param branche               Branche cible (normalisée en interne), peut être {@code null}
     * @param ageExtractor          Extrait l'âge d'adhésion d'une entrée du référentiel
     * @param brancheExtractor      Extrait la branche d'une entrée du référentiel
     * @param coefficientExtractor  Extrait la valeur du coefficient d'une entrée
     * @return Le coefficient résolu, ou {@code null} si non applicable
     */
    public static <T> Double resolveByAgeAndBranche(
        List<T> coefficients,
        Integer ageAdhesion,
        String branche,
        Function<T, Integer> ageExtractor,
        Function<T, String> brancheExtractor,
        Function<T, Double> coefficientExtractor) {

        if (coefficients == null || coefficients.isEmpty() || ageAdhesion == null) {
            return null;
        }

        String normalizedBranche = normalizeBranche(branche);

        return coefficients.stream()
            .filter(c -> c != null && ageExtractor.apply(c) != null)
            .filter(c -> normalizedBranche == null
                || normalizedBranche.equalsIgnoreCase(normalizeBranche(brancheExtractor.apply(c))))
            .filter(c -> ageExtractor.apply(c).equals(ageAdhesion))
            .map(coefficientExtractor)
            .filter(Objects::nonNull)
            .findFirst()
            .orElseGet(() -> coefficients.stream()
                .filter(c -> c != null && ageExtractor.apply(c) != null)
                .filter(c -> normalizedBranche == null
                    || normalizedBranche.equalsIgnoreCase(normalizeBranche(brancheExtractor.apply(c))))
                .min(Comparator.comparingInt(c -> Math.abs(ageExtractor.apply(c) - ageAdhesion)))
                .map(coefficientExtractor)
                .orElse(null));
    }

    /**
     * Résout le code de classe de risque associé à une CSP donnée, en
     * cherchant d'abord dans les correspondances par profession, puis, à
     * défaut, dans les correspondances par catégorie professionnelle.
     */
    public static String resolveClasseRisqueCode(
        String cspCode,
        String branche,
        List<ProfessionClasseRisqueMapping> professionMappings,
        List<CategorieProClasseRisqueMapping> categorieMappings) {
        String professionClasseRisque = findProfessionClasseRisque(cspCode, branche, professionMappings);
        return professionClasseRisque != null
            ? professionClasseRisque
            : findCategorieClasseRisque(cspCode, branche, categorieMappings);
    }

    public static String resolveClasseRisqueCode(
        String cspCode,
        List<ProfessionClasseRisqueMapping> professionMappings,
        List<CategorieProClasseRisqueMapping> categorieMappings) {
        return resolveClasseRisqueCode(cspCode, "VIE", professionMappings, categorieMappings);
    }

    private static String findProfessionClasseRisque(
        String cspCode,
        String branche,
        List<ProfessionClasseRisqueMapping> professionMappings) {
        if (cspCode == null || professionMappings == null) {
            return null;
        }
        String normalizedBranche = normalizeBranche(branche);
        return professionMappings.stream()
            .filter(mapping -> mapping != null && mapping.profession() != null && mapping.classeRisque() != null)
            .filter(mapping -> cspCode.equalsIgnoreCase(mapping.profession().code()))
            .filter(mapping -> mapping.branche() == null || normalizedBranche == null
                || normalizedBranche.equals(normalizeBranche(mapping.branche())))
            .map(mapping -> mapping.classeRisque().code())
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
    }

    private static String findCategorieClasseRisque(
        String cspCode,
        String branche,
        List<CategorieProClasseRisqueMapping> categorieMappings) {
        if (cspCode == null || categorieMappings == null) {
            return null;
        }
        String normalizedBranche = normalizeBranche(branche);
        return categorieMappings.stream()
            .filter(mapping -> mapping != null && mapping.categorieProfessionnelle() != null)
            .filter(mapping -> cspCode.equalsIgnoreCase(mapping.categorieProfessionnelle().code()))
            .map(mapping -> "NON_VIE".equals(normalizedBranche)
                ? mapping.classeRisqueAT()
                : mapping.classeRisqueDC())
            .filter(Objects::nonNull)
            .map(fr.hm.tarificateur.domain.model.Reference::code)
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
    }
}
