package fr.hm.tarificateur.domain.model;

import java.util.List;

public record ExonerationCotisations(
    Boolean exclueLemoine,
    List<ExonerationCotisationsCoefficient> coefficients
) {
}
