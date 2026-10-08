package fr.hm.tarificateur.domain.model;

/**
 * Classification de risque - regroupe tous les éléments de classification
 * du risque (classe de risque et branche d'assurance).
 *
 * @param classeRisqueCode    Code de la classe de risque (ex: CR1, CR2)
 * @param branche             Branche d'assurance (ex: VIE, NON_VIE)
 */
public record ClassificationRisque(
    String classeRisqueCode,
    String branche
) {}

