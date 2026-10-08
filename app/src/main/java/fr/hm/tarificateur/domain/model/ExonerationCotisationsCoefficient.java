package fr.hm.tarificateur.domain.model;

public record ExonerationCotisationsCoefficient(
    Boolean regimeLemoine,
    Double coefficient
) {
}
