package fr.hm.tarificateur.domain.model;

public record ObjetPretEligibilite(
        Reference objetPret,
        Boolean regimeLemoine,
        Boolean booEligible
) {}

