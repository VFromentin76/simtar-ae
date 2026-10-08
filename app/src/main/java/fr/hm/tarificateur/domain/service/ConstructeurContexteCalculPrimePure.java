package fr.hm.tarificateur.domain.service;

import fr.hm.tarificateur.domain.model.ClassificationRisque;
import fr.hm.tarificateur.domain.model.ContexteCalculPrimePure;
import fr.hm.tarificateur.domain.model.ContexteCoefficients;
import fr.hm.tarificateur.domain.model.ContextePret;
import fr.hm.tarificateur.domain.model.DetailConfig;
import fr.hm.tarificateur.domain.model.EligibilityLemoine;
import fr.hm.tarificateur.domain.model.ProfilEmprunteur;

/**
 * Builder fluide pour simplifier la construction de ContexteCalculPrimePure.
 *
 * Exemple d'utilisation:
 * <pre>
 * ContexteCalculPrimePure context = ConstructeurContexteCalculPrimePure.builder()
 *     .premiumBase(100.0)
 *     .detailConfig(detailConfig)
 *     .profilEmprunteur(new ProfilEmprunteur(true, true, false, 35))
 *     .contextePret(new ContextePret(250000.0, "PRET_AMORTISSABLE", "MAISON"))
 *     .classificationRisque(new ClassificationRisque("CR2", "VIE"))
 *     .build();
 * </pre>
 */
public class ConstructeurContexteCalculPrimePure {

    private double premiumBase;
    private DetailConfig detailConfig;
    private ProfilEmprunteur profilEmprunteur;
    private ContextePret contextePret;
    private ClassificationRisque classificationRisque;
    private ContexteCoefficients contexteCoefficients;
    private EligibilityLemoine eligibilityLemoine;
    private boolean exonerationCotisationsSouscrite;

    private ConstructeurContexteCalculPrimePure() {
    }

    public static ConstructeurContexteCalculPrimePure builder() {
        return new ConstructeurContexteCalculPrimePure();
    }

    public ConstructeurContexteCalculPrimePure premiumBase(double premiumBase) {
        this.premiumBase = premiumBase;
        return this;
    }

    public ConstructeurContexteCalculPrimePure detailConfig(DetailConfig detailConfig) {
        this.detailConfig = detailConfig;
        return this;
    }

    public ConstructeurContexteCalculPrimePure profilEmprunteur(ProfilEmprunteur profilEmprunteur) {
        this.profilEmprunteur = profilEmprunteur;
        return this;
    }

    public ConstructeurContexteCalculPrimePure contextePret(ContextePret contextePret) {
        this.contextePret = contextePret;
        return this;
    }

    public ConstructeurContexteCalculPrimePure classificationRisque(ClassificationRisque classificationRisque) {
        this.classificationRisque = classificationRisque;
        return this;
    }

    public ConstructeurContexteCalculPrimePure contexteCoefficients(ContexteCoefficients contexteCoefficients) {
        this.contexteCoefficients = contexteCoefficients;
        return this;
    }

    public ConstructeurContexteCalculPrimePure eligibilityLemoine(EligibilityLemoine eligibilityLemoine) {
        this.eligibilityLemoine = eligibilityLemoine;
        return this;
    }

    public ConstructeurContexteCalculPrimePure exonerationCotisationsSouscrite(boolean exonerationCotisationsSouscrite) {
        this.exonerationCotisationsSouscrite = exonerationCotisationsSouscrite;
        return this;
    }

    public ContexteCalculPrimePure build() {
        return new ContexteCalculPrimePure(
            premiumBase,
            detailConfig,
            profilEmprunteur,
            contextePret,
            classificationRisque,
            contexteCoefficients,
            eligibilityLemoine,
            exonerationCotisationsSouscrite
        );
    }
}

