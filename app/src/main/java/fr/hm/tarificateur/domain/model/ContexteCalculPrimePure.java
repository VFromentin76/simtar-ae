package fr.hm.tarificateur.domain.model;

/**
 * Contexte complet de calcul de prime - agrège tous les contextes thématiques
 * nécessaires pour calculer la prime pure totale d'une garantie vie.
 *
 * @param premiumBase                     Prime pure annuelle CI (DC/PTIA) de base
 * @param detailConfig                    Configuration détaillée (charges, taxes, coefficients généraux)
 * @param profilEmprunteur                Profil de l'emprunteur
 * @param contextePret                    Contexte du prêt
 * @param classificationRisque            Classification de risque
 * @param contexteCoefficients            Contexte des coefficients
 * @param eligibilityLemoine              Éligibilité et contexte Lemoine
 * @param exonerationCotisationsSouscrite Indique si l'exonération des cotisations est souscrite
 */
public record ContexteCalculPrimePure(
    double premiumBase,
    DetailConfig detailConfig,
    ProfilEmprunteur profilEmprunteur,
    ContextePret contextePret,
    ClassificationRisque classificationRisque,
    ContexteCoefficients contexteCoefficients,
    EligibilityLemoine eligibilityLemoine,
    boolean exonerationCotisationsSouscrite
) {}

