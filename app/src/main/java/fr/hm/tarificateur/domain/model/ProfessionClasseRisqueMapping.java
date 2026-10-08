package fr.hm.tarificateur.domain.model;

public record ProfessionClasseRisqueMapping(
        Reference profession,
        String branche,
        Reference classeRisque
) {
}
