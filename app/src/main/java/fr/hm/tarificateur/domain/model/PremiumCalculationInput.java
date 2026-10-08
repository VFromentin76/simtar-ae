package fr.hm.tarificateur.domain.model;

public record PremiumCalculationInput(
        Double loanAmount,
        Integer ageAdhesion,
        Integer loanDurationYears,
        String calculationMode
) {}

