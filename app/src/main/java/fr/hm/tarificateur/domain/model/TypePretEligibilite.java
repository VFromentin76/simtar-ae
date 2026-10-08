package fr.hm.tarificateur.domain.model;

public record TypePretEligibilite(
        Reference typePret,
        Boolean regimeLemoine,
        Boolean booEligible
) {}

