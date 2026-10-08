package fr.hm.tarificateur.domain.model;

/**
 * Contexte du prêt - regroupe tous les attributs spécifiques au prêt
 * (capital assuré, type, objet du prêt).
 *
 * @param capitalAssure       Capital assuré en euros
 * @param typePret            Code du type de prêt (ex: PRET_AMORTISSABLE)
 * @param objetPret           Code de l'objet du prêt (ex: MAISON, VOITURE)
 */
public record ContextePret(
    Double capitalAssure,
    String typePret,
    String objetPret
) {}

