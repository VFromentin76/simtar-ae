package fr.hm.tarificateur.domain.model;

import java.util.List;

public record CategorieProClasseRisqueMapping(
        Reference categorieProfessionnelle,
        Reference classeRisqueDC,
        Reference classeRisqueAT,
        List<CriterePro> criteres
) {
    public CategorieProClasseRisqueMapping(
        Reference categorieProfessionnelle,
        Reference classeRisque,
        List<CriterePro> criteres
    ) {
        this(categorieProfessionnelle, classeRisque, classeRisque, criteres);
    }
}
